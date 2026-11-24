import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("Buttons and links that answer back", () => {
  it("swaps the Order button's colours when the mouse is over it", () => {
    expect(value(".buy:hover", "background-color"), ".buy:hover should have background-color: var(--indigo)").toBe("var(--indigo)");
    expect(value(".buy:hover", "color"), ".buy:hover should have color: var(--off-white)").toBe("var(--off-white)");
  });

  it("draws a clear ring when the keyboard reaches an Order button", () => {
    expect(value(".buy:focus-visible", "outline"), ".buy:focus-visible should have outline: 3px solid var(--indigo)").toBe("3px solid var(--indigo)");
    expect(value(".buy:focus-visible", "outline-offset"), ".buy:focus-visible should have outline-offset: 2px").toBe("2px");
  });

  it("underlines the nav links for mouse and keyboard users alike", () => {
    expect(value("nav a:hover", "text-decoration"), "nav a:hover should have text-decoration: underline").toBe("underline");
    expect(value("nav a:focus-visible", "text-decoration"), "nav a:focus-visible should have text-decoration: underline").toBe("underline");
  });

  it("never removes the focus ring", () => {
    const hidden = rules().filter((rule) => /^(none|0|0px)$/.test(rule.decls.get("outline") ?? "")).map((rule) => rule.selector);
    expect(hidden, "keyboard users need to see where they are: don't set outline: none").toEqual([]);
  });
});
