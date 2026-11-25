package dev.cadence.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Needs the web workspace installed (npm ci in tracks/web/practice, which run.sh does); skipped without it. */
class NodeRunnerTest {

    private static final Path WORKSPACE = Path.of("../tracks/web/practice").toAbsolutePath().normalize();

    private NodeRunner runner;

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        runner = new NodeRunner(NodeRunner.nodeOnPath(), WORKSPACE, new RunLimits(Duration.ofSeconds(5), 256, 65536));
        assumeTrue(runner.ready(), "the web workspace is not installed");
    }

    private Path ticket(String source, String test) throws Exception {
        Files.createDirectories(dir.resolve("src"));
        Files.createDirectories(dir.resolve("test"));
        Files.writeString(dir.resolve("src/cart.jsx"), source);
        Files.writeString(dir.resolve("test/cart.test.jsx"), test);
        return dir;
    }

    private static final String TESTS = """
            import { describe, it, expect } from "vitest";
            import { render, screen } from "@testing-library/react";
            import { total, Left } from "../src/cart.jsx";
            describe("cart", () => {
              it("adds paise times quantity", () => { expect(total([{ paise: 15000, qty: 2 }]), "two coaster sets cost 30000 paise").toBe(30000); });
              it("shows how many are left", () => { render(<Left n={3} />); expect(screen.getByText("3 left")).toBeTruthy(); });
            });
            """;

    @Test
    void passingChecksArePassed() throws Exception {
        Path t = ticket("""
                export const total = (items) => items.reduce((s, i) => s + i.paise * i.qty, 0);
                export function Left({ n }) { return <span>{n} left</span>; }
                """, TESTS);
        var result = runner.run(t.resolve("src"), t.resolve("test"), runner.limits());
        assertEquals(TestRunResult.Outcome.PASSED, result.outcome(), result::output);
        assertEquals(2, result.tests().size());
        assertEquals("adds paise times quantity", result.tests().getFirst().name());
    }

    @Test
    void aWrongAnswerFailsWithTheCheckMessage() throws Exception {
        Path t = ticket("""
                export const total = (items) => items.length;
                export function Left({ n }) { return <span>{n} left</span>; }
                """, TESTS);
        var result = runner.run(t.resolve("src"), t.resolve("test"), runner.limits());
        assertEquals(TestRunResult.Outcome.FAILED, result.outcome(), result::output);
        var failed = result.tests().stream().filter(c -> !c.passed()).findFirst().orElseThrow();
        assertTrue(failed.message().contains("two coaster sets cost 30000 paise"), failed.message());
    }

    @Test
    void aSyntaxErrorIsReportedWithItsLine() throws Exception {
        Path t = ticket("export const total = (items) => {\n  return 1;\n}}\n", TESTS);
        var result = runner.run(t.resolve("src"), t.resolve("test"), runner.limits());
        assertEquals(TestRunResult.Outcome.COMPILE_ERROR, result.outcome());
        assertEquals(3, result.compileErrors().getFirst().line());
        assertEquals("cart.jsx", result.compileErrors().getFirst().file());
    }

    @Test
    void anEndlessLoopIsStopped() throws Exception {
        Path t = ticket("""
                export const total = () => { while (true) {} };
                export function Left() { return null; }
                """, TESTS);
        var fast = new RunLimits(Duration.ofSeconds(1), 256, 65536);
        var result = runner.run(t.resolve("src"), t.resolve("test"), fast);
        assertTrue(result.outcome() == TestRunResult.Outcome.TIMED_OUT || result.outcome() == TestRunResult.Outcome.FAILED,
                result.outcome() + " " + result.output());
    }

    @Test
    void aProgramPrintsWhatItReads() throws Exception {
        Files.createDirectories(dir.resolve("src"));
        Files.writeString(dir.resolve("src/main.js"), """
                import { readFileSync } from "node:fs";
                const minutes = Number(readFileSync(0, "utf8").trim());
                console.log(`Total: ₹${(minutes * 150).toFixed(2)}`);
                """);
        var result = runner.runMain(dir.resolve("src"), "main.js", runner.limits(), "3\n");
        assertEquals(ProgramResult.Status.COMPLETED, result.status(), result.output());
        assertEquals("Total: ₹450.00", result.output().strip());
    }

    @Test
    void aSnippetRuns() throws Exception {
        var result = runner.runSnippet("console.log([1, 2, 3].map((n) => n * 2).join(\" \"));", "");
        assertEquals("2 4 6", result.output().strip());
    }

    @Test
    void aTypeErrorInATypeScriptTicketIsAnErrorWithItsLine() throws Exception {
        Files.createDirectories(dir.resolve("src"));
        Files.createDirectories(dir.resolve("test"));
        Files.writeString(dir.resolve("src/cart.ts"), "export function total(paise: number, qty: number): number {\n  return paise * qty;\n}\nexport const label: string = 5;\n");
        Files.writeString(dir.resolve("test/cart.test.ts"), """
                import { it, expect } from "vitest";
                import { total } from "../src/cart.ts";
                it("multiplies", () => { expect(total(15000, 2), "two sets").toBe(30000); });
                """);
        var result = runner.run(dir.resolve("src"), dir.resolve("test"), runner.limits());
        assertEquals(TestRunResult.Outcome.COMPILE_ERROR, result.outcome(), result::output);
        assertEquals("cart.ts", result.compileErrors().getFirst().file());
        assertEquals(4, result.compileErrors().getFirst().line());
        assertTrue(result.compileErrors().getFirst().message().startsWith("TS2322"), result.compileErrors().getFirst().message());
    }

    @Test
    void aWellTypedTypeScriptTicketRunsItsChecks() throws Exception {
        Files.createDirectories(dir.resolve("src"));
        Files.createDirectories(dir.resolve("test"));
        Files.writeString(dir.resolve("src/cart.ts"), "export function total(paise: number, qty: number): number {\n  return paise * qty;\n}\n");
        Files.writeString(dir.resolve("test/cart.test.ts"), """
                import { it, expect } from "vitest";
                import { total } from "../src/cart.ts";
                it("multiplies paise by quantity", () => { expect(total(15000, 2), "two sets").toBe(30000); });
                """);
        var result = runner.run(dir.resolve("src"), dir.resolve("test"), runner.limits());
        assertEquals(TestRunResult.Outcome.PASSED, result.outcome(), result::output);
    }

    @Test
    void aTypeScriptSnippetRuns() throws Exception {
        var result = runner.runTypeScriptSnippet("const prices: number[] = [15000, 12000];\nconsole.log(prices.reduce((a: number, b: number) => a + b, 0));", "");
        assertEquals("27000", result.output().strip(), result.output());
    }
}
