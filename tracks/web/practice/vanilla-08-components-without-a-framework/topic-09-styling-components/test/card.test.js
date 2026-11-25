import { describe, it, expect } from "vitest";
import { productCard } from "../src/card.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 };
const tote = { id: "DC-1007", name: "Indigo tote bag", paise: 89900, stock: 0 };

describe("productCard", () => {
  it("is a product-card block", () => {
    const card = productCard(golu);
    expect(card.className, "the <article> should have exactly the class product-card").toBe("product-card");
    expect(card.dataset.id, "the card's data-id should be DC-1042").toBe("DC-1042");
  });

  it("gives each part its BEM element class", () => {
    const card = productCard(golu);
    expect(card.querySelector("h3")?.className, "the <h3> should be product-card__name").toBe("product-card__name");
    expect(card.querySelector(".product-card__price")?.textContent, "the price should be in .product-card__price").toBe("₹450.00");
    expect(card.querySelector("button")?.className, "the button should be product-card__add").toBe("product-card__add");
  });

  it("adds the sold-out modifier on top of the block", () => {
    const card = productCard(tote);
    expect(card.className, "a sold-out card should be product-card product-card--sold-out").toBe("product-card product-card--sold-out");
    expect(card.querySelector("button")?.disabled, "a sold-out card's button should be disabled").toBe(true);
  });

  it("uses no class outside the product-card block", () => {
    const card = productCard(tote);
    const classes = [card, ...card.querySelectorAll("*")].flatMap((element) => [...element.classList]);
    const strays = classes.filter((name) => !name.startsWith("product-card"));
    expect(strays, "every class in the card should start with product-card; these don't").toEqual([]);
  });
});
