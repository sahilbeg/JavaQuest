import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("Sizes and the soft grey", () => {
  it("makes the shop name twice the reader's text size", () => {
    expect(value("h1", "font-size"), "h1 should have font-size: 2rem").toBe("2rem");
  });

  it("makes the item codes a little smaller than normal text", () => {
    expect(value(".code", "font-size"), ".code should have font-size: 0.875rem").toBe("0.875rem");
  });

  it("colours the item codes soft grey with rgb()", () => {
    expect(value(".code", "color") ?? "", ".code should have color: rgb(90, 90, 90)").toMatch(/^rgb\(\s*90\s*,?\s*90\s*,?\s*90\s*\)$/);
  });

  it("sizes text in rem, never in px", () => {
    const pxSizes = rules().filter((rule) => (rule.decls.get("font-size") ?? "").endsWith("px"));
    expect(pxSizes.map((rule) => rule.selector), "font sizes should use rem, so they follow the reader's setting").toEqual([]);
  });
});
