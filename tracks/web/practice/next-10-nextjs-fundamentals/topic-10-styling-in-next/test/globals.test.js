import { describe, it, expect } from "vitest";
import { source, value, withoutComments } from "./css.js";

const GLOBALS = "app/globals.css";

describe("globals.css", () => {
  it("defines the three brand colours on :root", () => {
    expect(value(GLOBALS, ":root", "--marigold"), ":root should set --marigold: #f2a81d").toBe("#f2a81d");
    expect(value(GLOBALS, ":root", "--indigo"), ":root should set --indigo: #2e3a8c").toBe("#2e3a8c");
    expect(value(GLOBALS, ":root", "--off-white"), ":root should set --off-white: #faf7f0").toBe("#faf7f0");
  });

  it("gives every page the off-white background and indigo text", () => {
    const background = value(GLOBALS, "body", "background") ?? value(GLOBALS, "body", "background-color");
    expect(background, "body should have background: var(--off-white)").toBe("var(--off-white)");
    expect(value(GLOBALS, "body", "color"), "body should have color: var(--indigo)").toBe("var(--indigo)");
  });

  it("turns links indigo instead of browser blue", () => {
    expect(value(GLOBALS, "a", "color"), "a rule for a should set color: var(--indigo)").toBe("var(--indigo)");
  });

  it("is imported once, in the root layout", () => {
    expect(
      /import\s+["']\.\/globals\.css["']/.test(withoutComments(source("app/layout.jsx"))),
      "src/app/layout.jsx should keep import \"./globals.css\";",
    ).toBe(true);
  });
});
