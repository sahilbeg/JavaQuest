import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("Dhaaga's brand colours", () => {
  it("gives the page an Off-white background and Indigo text", () => {
    expect(value("body", "background-color"), "body should have background-color: #faf7f0 (Off-white)").toBe("#faf7f0");
    expect(value("body", "color"), "body should have color: #2e3a8c (Indigo)").toBe("#2e3a8c");
  });

  it("puts the header on an Indigo band with Off-white text", () => {
    expect(value(".site-header", "background-color"), ".site-header should have background-color: #2e3a8c").toBe("#2e3a8c");
    expect(value(".site-header", "color"), ".site-header should have color: #faf7f0").toBe("#faf7f0");
  });

  it("colours the headings Indigo instead of darkblue", () => {
    expect(value("h2", "color"), "h2 should be the brand Indigo, #2e3a8c").toBe("#2e3a8c");
    expect(value("h3", "color"), "h3 should be the brand Indigo, #2e3a8c").toBe("#2e3a8c");
  });

  it("makes the nav links Off-white so they show on the Indigo header", () => {
    expect(value("nav a", "color"), "nav a should have color: #faf7f0").toBe("#faf7f0");
  });

  it("turns every Order link into a Marigold patch with Indigo text", () => {
    expect(value(".buy", "background-color"), ".buy should have background-color: #f2a81d (Marigold)").toBe("#f2a81d");
    expect(value(".buy", "color"), ".buy should have color: #2e3a8c, dark text on the bright patch").toBe("#2e3a8c");
  });

  it("has no stand-in colour names left", () => {
    const leftovers = rules().filter((rule) => [...rule.decls.values()].some((v) => v === "darkblue" || v === "darkred"));
    expect(leftovers.map((rule) => rule.selector), "darkblue and darkred should all be swapped for brand colours").toEqual([]);
  });
});
