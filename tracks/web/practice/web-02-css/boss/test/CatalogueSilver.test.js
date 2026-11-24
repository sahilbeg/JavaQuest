import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("Silver: a phone-first product grid", () => {
  it("keeps the products in a grid", () => {
    expect(value(".products", "display"), ".products should keep display: grid").toBe("grid");
  });

  it("gives phones one column in the base rule", () => {
    const columns = value(".products", "grid-template-columns");
    expect(columns === undefined || columns === "1fr", `the base .products rule has grid-template-columns: ${columns}; phones need one column, so leave it out (or use 1fr)`).toBe(true);
  });

  it("adds more columns inside a min-width media query", () => {
    const wider = rules().filter(
      (rule) => rule.selector === ".products" && rule.media !== null && /min-width/i.test(rule.media) && rule.decls.has("grid-template-columns"),
    );
    expect(wider.length, "add @media (min-width: 40rem) { .products { grid-template-columns: repeat(2, 1fr); } }").toBeGreaterThan(0);
  });

  it("uses no max-width media queries", () => {
    const desktopFirst = rules().filter((rule) => rule.media !== null && /max-width/i.test(rule.media));
    expect(desktopFirst.map((rule) => `${rule.media} ${rule.selector}`), "phone first means min-width queries only").toEqual([]);
  });
});
