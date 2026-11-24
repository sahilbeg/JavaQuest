import { describe, it, expect } from "vitest";
import { value } from "./css.js";

describe("The box model on the catalogue", () => {
  it("measures every element with border-box", () => {
    expect(value("*", "box-sizing"), "add * { box-sizing: border-box; }").toBe("border-box");
  });

  it("removes the browser's margin around the page", () => {
    expect(value("body", "margin") ?? "", "body should have margin: 0").toMatch(/^0(px)?$/);
  });

  it("pads the header 1rem top and bottom and 2rem left and right", () => {
    expect(value(".site-header", "padding"), ".site-header should have padding: 1rem 2rem").toBe("1rem 2rem");
  });

  it("removes the list's bullet room", () => {
    expect(value(".products", "padding") ?? "", ".products should have padding: 0").toMatch(/^0(px)?$/);
  });

  it("gives each product room inside its frame", () => {
    expect(value(".product", "padding"), ".product should have padding: 1rem").toBe("1rem");
  });

  it("frames each product in Indigo with soft corners", () => {
    expect(value(".product", "border"), ".product should have border: 2px solid #2e3a8c (width, style, colour)").toBe("2px solid #2e3a8c");
    expect(value(".product", "border-radius"), ".product should have border-radius: 8px").toBe("8px");
  });

  it("leaves a gap below each card", () => {
    expect(value(".product", "margin-bottom"), ".product should have margin-bottom: 1rem").toBe("1rem");
  });
});
