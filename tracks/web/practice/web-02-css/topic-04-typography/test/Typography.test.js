import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

const GENERIC = /(serif|sans-serif|monospace|system-ui|cursive|fantasy)$/;

describe("Dhaaga's fonts and text", () => {
  it("sets the page font to the phone's own font, with sans-serif as the backup", () => {
    expect(value("body", "font-family") ?? "", "body should have font-family: system-ui, sans-serif").toMatch(/^system-ui,\s*sans-serif$/);
  });

  it("gives body text comfortable lines", () => {
    expect(value("body", "line-height"), "body should have line-height: 1.5 (a plain number, no unit)").toBe("1.5");
  });

  it("gives the h1 and h2 headings a warm serif", () => {
    expect(value("h1", "font-family") ?? "", "h1 should have font-family: Georgia, serif").toMatch(/^"?georgia"?,\s*serif$/);
    expect(value("h2", "font-family") ?? "", "h2 should have font-family: Georgia, serif").toMatch(/^"?georgia"?,\s*serif$/);
  });

  it("shows section titles in spaced capitals", () => {
    expect(value("h2", "text-transform"), "h2 should have text-transform: uppercase").toBe("uppercase");
    expect(value("h2", "letter-spacing"), "h2 should have letter-spacing: 0.1em").toBe("0.1em");
  });

  it("slants the tagline", () => {
    expect(value(".tagline", "font-style"), ".tagline should have font-style: italic").toBe("italic");
  });

  it("removes the underline from the nav links", () => {
    expect(value("nav a", "text-decoration"), "nav a should have text-decoration: none").toBe("none");
  });

  it("makes prices bigger", () => {
    expect(value(".price", "font-size"), ".price should have font-size: 1.25rem").toBe("1.25rem");
  });

  it("ends every font stack with a generic family", () => {
    const stacks = rules().filter((rule) => rule.decls.has("font-family"));
    expect(stacks.length, "the stylesheet should set font-family somewhere").toBeGreaterThan(0);
    const unsafe = stacks.filter((rule) => !GENERIC.test(rule.decls.get("font-family"))).map((rule) => rule.selector);
    expect(unsafe, "each font-family list should end with serif, sans-serif, monospace or system-ui").toEqual([]);
  });
});
