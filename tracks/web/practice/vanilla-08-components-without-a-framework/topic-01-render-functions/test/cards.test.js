import { describe, it, expect, vi } from "vitest";
import { productCard, productList } from "../src/cards.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 };
const keychain = { id: "DC-1013", name: "Rani pink keychain", paise: 12000, stock: 9 };
const tote = { id: "DC-1007", name: "Indigo tote bag", paise: 89900, stock: 0 };

describe("productCard", () => {
  it("builds an li.product with the item's id", () => {
    const card = productCard(golu, vi.fn());
    expect(card.tagName, "the card should be an <li>").toBe("LI");
    expect(card.classList.contains("product"), "the card should have the class product").toBe(true);
    expect(card.dataset.id, "the card's data-id should be DC-1042").toBe("DC-1042");
  });

  it("shows the name and the price in rupees", () => {
    const card = productCard(golu, vi.fn());
    expect(card.querySelector("h3")?.textContent, "the <h3> should say Golu the elephant").toBe("Golu the elephant");
    expect(card.querySelector(".price")?.textContent, "45000 paise should show as ₹450.00 in p.price").toBe("₹450.00");
  });

  it("calls onAdd with the id when Add to cart is pressed", () => {
    const onAdd = vi.fn();
    const card = productCard(golu, onAdd);
    const button = card.querySelector("button.add");
    expect(button?.textContent, "the button should say Add to cart").toBe("Add to cart");
    expect(button?.disabled, "an item in stock should have an enabled button").toBe(false);
    button?.click();
    expect(onAdd.mock.calls, "pressing Add to cart should call onAdd(\"DC-1042\") once").toEqual([["DC-1042"]]);
  });

  it("marks a sold-out item and disables its button", () => {
    const card = productCard(tote, vi.fn());
    expect(card.className, "a sold-out card should have the classes product sold-out").toBe("product sold-out");
    expect(card.querySelector("button.add")?.disabled, "a sold-out item's button should be disabled").toBe(true);
    expect(card.querySelector("button.add")?.textContent, "a sold-out item's button should say Sold out").toBe("Sold out");
  });

  it("keeps a name with tags as plain text", () => {
    const card = productCard({ ...golu, name: "<img src=x>Golu" }, vi.fn());
    expect(card.querySelector("h3")?.textContent, "the name should show exactly as written").toBe("<img src=x>Golu");
    expect(card.querySelector("img"), "a name must never turn into HTML").toBeNull();
  });
});

describe("productList", () => {
  it("draws one card per product, in order", () => {
    const list = productList([golu, keychain, tote], vi.fn());
    expect(list.tagName, "productList should return a <ul>").toBe("UL");
    expect(list.className, "the list should have the class products").toBe("products");
    const ids = [...list.querySelectorAll("li.product")].map((card) => card.dataset.id);
    expect(ids, "the cards should be Golu, the keychain and the tote bag, in that order").toEqual(["DC-1042", "DC-1013", "DC-1007"]);
  });

  it("passes onAdd down to every card", () => {
    const onAdd = vi.fn();
    const list = productList([golu, keychain], onAdd);
    list.querySelector("li[data-id=\"DC-1013\"] button.add")?.click();
    expect(onAdd.mock.calls, "the keychain's button should call onAdd(\"DC-1013\")").toEqual([["DC-1013"]]);
  });

  it("shows No items match. when there are no products", () => {
    const list = productList([], vi.fn());
    expect(list.querySelectorAll("li").length, "an empty catalogue should hold exactly one <li>").toBe(1);
    expect(list.querySelector("li.empty")?.textContent, "the one <li> should be li.empty saying No items match.").toBe("No items match.");
  });
});
