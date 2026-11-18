package dev.cadence.lesson;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LessonRendererTest {

    private final LessonRenderer renderer = new LessonRenderer();

    @Test
    void runnableJavaBlockBecomesASnippet() {
        String html = renderer.render("```java run\nSystem.out.println(\"<hi>\");\n```\n");

        assertTrue(html.contains("<div class=\"snippet\" data-runnable=\"\">"), html);
        assertTrue(html.contains("class=\"language-java\""), html);
        assertTrue(html.contains("&lt;hi&gt;"), "code must be escaped: " + html);
    }

    @Test
    void codeKeepsItsIndentation() {
        String html = renderer.render("```java\nclass A {\n    void b() {\n        c();\n    }\n}\n```\n");

        assertTrue(html.contains("class A {\n    void b() {\n        c();\n    }\n}"), html);
    }

    @Test
    void plainAndNoCompileJavaBlocksAreNotRunnable() {
        String html = renderer.render("```java\nint a;\n```\n\n```java nocompile\nint b\n```\n");

        assertFalse(html.contains("data-runnable"), html);
        assertTrue(html.contains("int b"), html);
    }

    @Test
    void calloutRendersItsMarkdownInsideAnAside() {
        String html = renderer.render("```callout analogy\nA class is **like** a folder.\n```\n");

        assertTrue(html.contains("<aside class=\"callout callout-analogy\" data-kind=\"analogy\">"), html);
        assertTrue(html.contains("<strong>like</strong>"), html);
    }

    @Test
    void unknownCalloutKindFallsBackToTip() {
        assertTrue(renderer.render("```callout shout\nHey\n```\n").contains("callout-tip"));
    }

    @Test
    void mermaidStaysAsEscapedSourceForTheBrowser() {
        String html = renderer.render("```mermaid\nflowchart LR\n  A --> B\n```\n");

        assertTrue(html.contains("<pre class=\"mermaid\">flowchart LR\n  A --&gt; B\n</pre>"), html);
    }

    @Test
    void headingsGetIdsForReteachLinks() {
        String html = renderer.render("## Worked example\n");

        assertTrue(html.contains("id=\"worked-example\""), html);
    }

    @Test
    void rawHtmlInLessonsIsEscaped() {
        assertFalse(renderer.render("<script>alert(1)</script>\n").contains("<script>"));
    }

    @Test
    void crashSnippetIsStillRunnable() {
        assertTrue(renderer.render("```java run crash\nint x;\n```\n").contains("data-runnable"));
    }

    @Test
    void animateBlockCarriesItsSpecAndATextFallback() {
        String html = renderer.render("""
                ```animate
                title: A box that changes
                kind: code
                code: |
                  int x = 3;
                  System.out.println(x);
                steps:
                  - { line: 1, vars: { x: 3 }, say: "x holds <3>." }
                  - { line: 2, out: "3", say: It prints 3. }
                ```
                """);

        assertTrue(html.contains("<figure class=\"animation\" data-spec=\"{"), html);
        assertTrue(html.contains("<li>x holds &lt;3&gt;.</li>"), "captions are escaped text: " + html);
        assertFalse(html.contains("<3>"), html);
    }

    @Test
    void brokenAnimateBlockShowsTheProblem() {
        String html = renderer.render("```animate\ntitle: x\nkind: code\ncode: int a;\nsteps: []\n```\n");

        assertTrue(html.contains("class=\"animation-error\""), html);
        assertTrue(html.contains("steps needs 2 to"), html);
    }
}
