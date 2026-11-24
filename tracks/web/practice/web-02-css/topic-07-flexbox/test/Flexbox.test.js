import { describe, it, expect } from "vitest";
import { value } from "./css.js";

describe("The header in one row", () => {
  it("puts the brand on the left and the nav on the right", () => {
    expect(value(".site-header", "display"), ".site-header should have display: flex").toBe("flex");
    expect(value(".site-header", "justify-content"), ".site-header should have justify-content: space-between").toBe("space-between");
  });

  it("centres the brand and the nav up and down", () => {
    expect(value(".site-header", "align-items"), ".site-header should have align-items: center").toBe("center");
  });

  it("lines up the nav links with a gap between them", () => {
    expect(value("nav", "display"), "nav should have display: flex").toBe("flex");
    expect(value("nav", "gap"), "nav should have gap: 1rem").toBe("1rem");
  });
});

describe("The product cards side by side", () => {
  it("puts the cards in a row that wraps", () => {
    expect(value(".products", "display"), ".products should have display: flex").toBe("flex");
    expect(value(".products", "flex-wrap"), ".products should have flex-wrap: wrap, so cards move to a new row").toBe("wrap");
    expect(value(".products", "gap"), ".products should have gap: 1rem").toBe("1rem");
  });

  it("starts each card at 14rem and lets it grow and shrink", () => {
    expect(value(".product", "flex"), ".product should have flex: 1 1 14rem (grow, shrink, basis)").toBe("1 1 14rem");
  });

  it("stacks each card's parts in a column", () => {
    expect(value(".product", "display"), ".product should have display: flex").toBe("flex");
    expect(value(".product", "flex-direction"), ".product should have flex-direction: column").toBe("column");
  });

  it("pushes every Order button to the bottom of its card", () => {
    expect(value(".buy", "margin-top"), ".buy should have margin-top: auto").toBe("auto");
    expect(value(".buy", "align-self"), ".buy should have align-self: flex-start, so it doesn't stretch across the card").toBe("flex-start");
  });
});
