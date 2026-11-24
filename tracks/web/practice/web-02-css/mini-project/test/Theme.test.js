import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

const THEME_RULES = [":root", ".theme-diwali", "body.theme-diwali"];
const THEME_HEX = /#(2e3a8c|f2a81d|faf7f0|8c1d40|ffc93c|fff4e0|4a1020)/;

/** A token set by the Diwali theme rule, written as .theme-diwali or body.theme-diwali. */
function diwali(token) {
  return value(".theme-diwali", token) ?? value("body.theme-diwali", token);
}

describe("The everyday tokens", () => {
  it("sets the four tokens on :root", () => {
    expect(value(":root", "--brand"), ":root should set --brand: #2e3a8c").toBe("#2e3a8c");
    expect(value(":root", "--accent"), ":root should set --accent: #f2a81d").toBe("#f2a81d");
    expect(value(":root", "--page"), ":root should set --page: #faf7f0").toBe("#faf7f0");
    expect(value(":root", "--ink"), ":root should set --ink: #2e3a8c").toBe("#2e3a8c");
  });

  it("drops the old colour-named properties", () => {
    const old = rules()
      .filter((rule) => [...rule.decls.keys(), ...rule.decls.values()].some((text) => /--(marigold|indigo|off-white)\b/.test(text)))
      .map((rule) => rule.selector);
    expect(old, "--marigold, --indigo and --off-white should all become --brand, --accent, --page or --ink").toEqual([]);
  });
});

describe("The Diwali theme", () => {
  it("sets the four tokens again on .theme-diwali", () => {
    expect(diwali("--brand"), ".theme-diwali should set --brand: #8c1d40").toBe("#8c1d40");
    expect(diwali("--accent"), ".theme-diwali should set --accent: #ffc93c").toBe("#ffc93c");
    expect(diwali("--page"), ".theme-diwali should set --page: #fff4e0").toBe("#fff4e0");
    expect(diwali("--ink"), ".theme-diwali should set --ink: #4a1020").toBe("#4a1020");
  });

  it("adds a dashed gold line under the header, only in the Diwali theme", () => {
    expect(value(".theme-diwali .site-header", "border-bottom"), ".theme-diwali .site-header should have border-bottom: 4px dashed var(--accent)").toBe("4px dashed var(--accent)");
    expect(value(".site-header", "border-bottom"), "the everyday header has no dashed line: put it in .theme-diwali .site-header only").toBeUndefined();
  });
});

describe("Components that read the tokens", () => {
  it("colours the page with --page and --ink", () => {
    expect(value("body", "background-color"), "body should have background-color: var(--page)").toBe("var(--page)");
    expect(value("body", "color"), "body should have color: var(--ink)").toBe("var(--ink)");
  });

  it("colours the header band with --brand and --page", () => {
    expect(value(".site-header", "background-color"), ".site-header should have background-color: var(--brand)").toBe("var(--brand)");
    expect(value(".site-header", "color"), ".site-header should have color: var(--page)").toBe("var(--page)");
  });

  it("colours the prices with --brand", () => {
    expect(value(".price", "color"), ".price should have color: var(--brand)").toBe("var(--brand)");
  });

  it("colours the Order buttons with --accent and --ink, and --brand on hover", () => {
    expect(value(".buy", "background-color"), ".buy should have background-color: var(--accent)").toBe("var(--accent)");
    expect(value(".buy", "color"), ".buy should have color: var(--ink)").toBe("var(--ink)");
    expect(value(".buy:hover", "background-color"), ".buy:hover should have background-color: var(--brand)").toBe("var(--brand)");
    expect(value(".buy:hover", "color"), ".buy:hover should have color: var(--page)").toBe("var(--page)");
  });

  it("types theme colours only inside the two theme rules", () => {
    const copies = rules()
      .filter((rule) => !THEME_RULES.includes(rule.selector))
      .filter((rule) => [...rule.decls.values()].some((v) => THEME_HEX.test(v)))
      .map((rule) => rule.selector);
    expect(copies, "these rules type a theme colour: read a token with var() instead").toEqual([]);
  });
});
