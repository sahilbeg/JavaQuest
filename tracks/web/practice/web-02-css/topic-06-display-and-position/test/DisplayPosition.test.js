import { describe, it, expect } from "vitest";
import { value } from "./css.js";

describe("The badge, the buttons and the header", () => {
  it("keeps the header stuck to the top while scrolling", () => {
    expect(value(".site-header", "position"), ".site-header should have position: sticky").toBe("sticky");
    expect(value(".site-header", "top") ?? "", ".site-header should have top: 0, or sticky has nowhere to stick").toMatch(/^0(px)?$/);
  });

  it("makes each product card the anchor for its badge", () => {
    expect(value(".product", "position"), ".product should have position: relative").toBe("relative");
  });

  it("pins the New badge 0.5rem from the card's top-right corner", () => {
    expect(value(".badge", "position"), ".badge should have position: absolute").toBe("absolute");
    expect(value(".badge", "top"), ".badge should have top: 0.5rem").toBe("0.5rem");
    expect(value(".badge", "right"), ".badge should have right: 0.5rem").toBe("0.5rem");
  });

  it("turns the Order links into real buttons", () => {
    expect(value(".buy", "display"), ".buy should have display: inline-block, so its padding counts").toBe("inline-block");
    expect(value(".buy", "padding"), ".buy should have padding: 0.5rem 1rem").toBe("0.5rem 1rem");
  });

  it("removes the Order button from sold-out cards", () => {
    expect(value(".sold-out .buy", "display"), ".sold-out .buy should have display: none").toBe("none");
  });
});
