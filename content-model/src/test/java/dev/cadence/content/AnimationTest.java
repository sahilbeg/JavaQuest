package dev.cadence.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AnimationTest {

    private static final String CODE = """
            title: A box that changes
            kind: code
            code: |
              int x = 3;
              x = x + 2;
              System.out.println(x);
            steps:
              - line: 1
                vars: { x: 3 }
                say: Java makes a box called x and puts 3 in it.
              - line: 2
                vars: { x: 5 }
                say: It reads 3, adds 2, and puts 5 back in the same box.
              - line: 3
                out: "5"
                say: println shows what is in the box.
            """;

    @Test
    void codeAnimationParsesAndKnowsItsOutput() {
        Animation animation = Animation.parse(CODE);
        assertEquals(Animation.Kind.CODE, animation.kind());
        assertEquals(3, animation.steps().size());
        assertEquals("5\n", animation.expectedOutput());
        assertEquals(Map.of("x", 5), animation.varsAfter(1));
        assertTrue(animation.toJson().contains("\"kind\":\"code\""), animation.toJson());
    }

    @Test
    void objectsAndReferences() {
        Animation animation = Animation.parse("""
                title: Two names, one bike
                kind: code
                code: |
                  Bike a = new Bike("B-1042");
                  Bike b = a;
                steps:
                  - line: 1
                    objects: { b1: { type: Bike, fields: { id: '"B-1042"' } } }
                    vars: { a: "@b1" }
                    say: new makes one Bike object. a points to it.
                  - line: 2
                    vars: { b: "@b1" }
                    say: b = a copies the arrow, not the bike.
                """);
        assertEquals(Map.of("a", "@b1", "b", "@b1"), animation.varsAfter(1));
    }

    @Test
    void tildeRemovesAVariableOrAnObject() {
        Animation animation = Animation.parse("""
                title: Boxes come and go
                kind: code
                code: |
                  int x = 1;
                  int y = 2;
                steps:
                  - line: 1
                    vars: { x: 1 }
                    objects: { s1: { type: Spare, fields: { n: 1 } } }
                    say: One.
                  - line: 2
                    vars: { x: ~, y: 2 }
                    objects:
                      s1: ~
                    say: Two.
                """);
        assertEquals(Map.of("y", 2), animation.varsAfter(1));
        assertTrue(animation.toJson().contains("\"s1\":null"), animation.toJson());
    }

    @Test
    void sceneAnimationParses() {
        Animation animation = Animation.parse("""
                title: The CPU asks memory
                kind: scene
                nodes:
                  - { id: cpu, label: CPU, emoji: "🧠" }
                  - { id: ram, label: Memory, value: "x = 3" }
                links:
                  - { from: cpu, to: ram, label: bus }
                steps:
                  - say: The CPU needs x.
                    focus: cpu
                  - say: It asks memory, and memory answers 3.
                    send: [{ from: cpu, to: ram, label: "x?" }, { from: ram, to: cpu, label: "3", back: true }]
                """);
        assertEquals(2, animation.nodes().size());
        assertEquals("bus", animation.links().getFirst().label());
        assertEquals(2, animation.steps().get(1).send().size());
    }

    @Test
    void mistakesAreNamed() {
        assertProblem("line 9 is outside the code", CODE.replace("- line: 3", "- line: 9"));
        assertProblem("say is required", CODE.replace("    say: println shows what is in the box.\n", ""));
        assertProblem("steps needs 2 to", """
                title: Too short
                kind: code
                code: int x = 1;
                steps:
                  - { line: 1, say: Only one step. }
                """);
        assertProblem("no node called 'disk'", """
                title: Bad focus
                kind: scene
                nodes: [{ id: cpu, label: CPU }]
                steps:
                  - { say: One., focus: cpu }
                  - { say: Two., focus: disk }
                """);
        assertProblem("'@b2' points to an object that does not exist yet", """
                title: Dangling arrow
                kind: code
                code: Bike a = null;
                steps:
                  - { line: 1, say: One., vars: { a: "@b2" } }
                  - { line: 1, say: Two. }
                """);
        assertProblem("belong to scene animations", CODE.replace("vars: { x: 3 }", "focus: x"));
    }

    private static void assertProblem(String expected, String yaml) {
        var e = assertThrows(IllegalArgumentException.class, () -> Animation.parse(yaml));
        assertTrue(e.getMessage().contains(expected), () -> "expected '" + expected + "' in: " + e.getMessage());
    }
}
