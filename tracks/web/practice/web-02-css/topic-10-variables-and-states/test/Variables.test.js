import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

const BRAND_HEX = /#(f2a81d|2e3a8c|faf7f0)/;

describe("The brand colours in one place", () => {
  it("defines the three brand colours on :root", () => {
    expect(value(":root", "--marigold"), ":root should set --marigold: #f2a81d").toBe("#f2a81d");
    expect(value(":root", "--indigo"), ":root should set --indigo: #2e3a8c").toBe("#2e3a8c");
    expect(value(":root", "--off-white"), ":root should set --off-white: #faf7f0").toBe("#faf7f0");
  });

  it("colours the page with the custom properties", () => {
    expect(value("body", "background-color"), "body should have background-color: var(--off-white)").toBe("var(--off-white)");
    expect(value("body", "color"), "body should have color: var(--indigo)").toBe("var(--indigo)");
  });

  it("colours the header band with the custom properties", () => {
    expect(value(".site-header", "background-color"), ".site-header should have background-color: var(--indigo)").toBe("var(--indigo)");
    expect(value(".site-header", "color"), ".site-header should have color: var(--off-white)").toBe("var(--off-white)");
  });

  it("colours the Order buttons with the custom properties", () => {
    expect(value(".buy", "background-color"), ".buy should have background-color: var(--marigold)").toBe("var(--marigold)");
    expect(value(".buy", "color"), ".buy should have color: var(--indigo)").toBe("var(--indigo)");
  });

  it("writes each brand hex code only on :root", () => {
    const copies = rules()
      .filter((rule) => rule.selector !== ":root")
      .filter((rule) => [...rule.decls.values()].some((v) => BRAND_HEX.test(v)))
      .map((rule) => rule.selector);
    expect(copies, "these rules still type a brand hex code: use var(--marigold), var(--indigo) or var(--off-white)").toEqual([]);
  });
});
