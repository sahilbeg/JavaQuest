import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("Bronze: the page fits a small phone", () => {
  it("gives main no fixed width in px", () => {
    const width = value("main", "width");
    expect(width === undefined || !width.endsWith("px"), `main has width: ${width}; delete it, a fixed width can't shrink to fit a phone`).toBe(true);
  });

  it("caps main at 1200px with max-width instead", () => {
    expect(value("main", "max-width") ?? "", "main should have max-width: 1200px").toMatch(/^(1200px|75rem)$/);
  });

  it("keeps main centred on wide screens", () => {
    expect(value("main", "margin") ?? "", "main should keep margin: 0 auto").toMatch(/^0(px)? auto$/);
  });

  it("sets no width wider than a phone outside a media query", () => {
    const tooWide = rules()
      .filter((rule) => rule.media === null)
      .filter((rule) => {
        const match = /^(\d+(?:\.\d+)?)px$/.exec(rule.decls.get("width") ?? "");
        return match !== null && Number(match[1]) > 320;
      })
      .map((rule) => `${rule.selector} { width: ${rule.decls.get("width")} }`);
    expect(tooWide, "these boxes are wider than a small phone: use max-width instead").toEqual([]);
  });
});
