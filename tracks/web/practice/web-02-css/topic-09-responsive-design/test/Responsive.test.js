import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

const WIDE = /^\(\s*min-width:\s*48rem\s*\)$/i;

/** The value a selector sets inside @media (min-width: 48rem), the last rule winning. */
function wide(selector, property) {
  let found;
  for (const rule of rules()) {
    if (rule.media && WIDE.test(rule.media) && rule.selector === selector && rule.decls.has(property)) found = rule.decls.get(property);
  }
  return found;
}

describe("Phone first: the base rules", () => {
  it("stacks the brand and the nav on phones", () => {
    expect(value(".site-header", "flex-direction"), ".site-header should have flex-direction: column outside any media query").toBe("column");
    expect(value(".site-header", "justify-content"), "move justify-content: space-between into the media query").toBeUndefined();
  });

  it("gives phones a slightly smaller shop name", () => {
    expect(value("h1", "font-size"), "h1 should have font-size: 1.75rem outside any media query").toBe("1.75rem");
  });

  it("stacks the two shops in one column on phones", () => {
    expect(value(".places", "display"), ".places should have display: grid").toBe("grid");
    expect(value(".places", "gap"), ".places should have gap: 1rem").toBe("1rem");
    expect(value(".places", "grid-template-columns"), "phones get one column, so the base .places rule sets no grid-template-columns").toBeUndefined();
  });
});

describe("Wider screens: @media (min-width: 48rem)", () => {
  it("has a min-width: 48rem media query", () => {
    expect(rules().some((rule) => rule.media && WIDE.test(rule.media)), "add a block @media (min-width: 48rem) { ... }").toBe(true);
  });

  it("puts the header back in one row", () => {
    expect(wide(".site-header", "flex-direction"), "inside the media query, .site-header should have flex-direction: row").toBe("row");
    expect(wide(".site-header", "justify-content"), "inside the media query, .site-header should have justify-content: space-between").toBe("space-between");
    expect(wide(".site-header", "align-items"), "inside the media query, .site-header should have align-items: center").toBe("center");
  });

  it("makes the shop name bigger", () => {
    expect(wide("h1", "font-size"), "inside the media query, h1 should have font-size: 2.5rem").toBe("2.5rem");
  });

  it("puts the two shops side by side", () => {
    expect(wide(".places", "grid-template-columns") ?? "", "inside the media query, .places should have grid-template-columns: 1fr 1fr").toMatch(/^(1fr 1fr|repeat\(\s*2\s*,\s*1fr\s*\))$/);
  });

  it("uses no max-width media queries", () => {
    const desktopFirst = rules().filter((rule) => rule.media && /max-width/i.test(rule.media));
    expect(desktopFirst.map((rule) => `${rule.media} ${rule.selector}`), "phone first means min-width queries only").toEqual([]);
  });
});
