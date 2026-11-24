import { describe, it, expect, beforeEach, vi } from "vitest";
import { stockText, renderProduct } from "../src/productCard.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 2 };
const booties = { id: "DC-1077", name: "Baby booties", paise: 35000, stock: 0 };
const coasters = { id: "DC-1003", name: "Coaster set (4)", paise: 15000, stock: 12 };

// Every test starts from the same clean page: one empty shelf.
beforeEach(() => {
  document.body.innerHTML = `<section id="shelf"></section>`;
});

const shelf = () => document.querySelector("#shelf");
const find = (selector) => document.querySelector(`#shelf ${selector}`);

describe("stockText", () => {
  it("says Sold out when none are left", () => {
    expect(stockText(0), "stock 0 should read Sold out").toBe("Sold out");
  });

  it("warns when only a few are left", () => {
    expect(stockText(1), "stock 1 should read Only 1 left").toBe("Only 1 left");
    expect(stockText(3), "stock 3 should read Only 3 left").toBe("Only 3 left");
  });

  it("says In stock when there are more than three", () => {
    expect(stockText(4), "stock 4 should read In stock").toBe("In stock");
    expect(stockText(25), "stock 25 should read In stock").toBe("In stock");
  });
});

describe("renderProduct", () => {
  it("draws one product card with the item id", () => {
    renderProduct(shelf(), golu, vi.fn());
    const cards = document.querySelectorAll("#shelf article.product");
    expect(cards.length, "the shelf should hold one <article class=\"product\">").toBe(1);
    expect(cards[0]?.dataset.id, "the card's data-id should be DC-1042").toBe("DC-1042");
  });

  it("shows the name, the price in rupees and the stock", () => {
    renderProduct(shelf(), golu, vi.fn());
    expect(find("h3")?.textContent, "the <h3> should hold the product name").toBe("Golu the elephant");
    expect(find(".price")?.textContent, "45000 paise should show as ₹450.00 in .price").toBe("₹450.00");
    expect(find(".stock")?.textContent, "a stock of 2 should show Only 2 left in .stock").toBe("Only 2 left");
  });

  it("has an Add to cart button", () => {
    renderProduct(shelf(), coasters, vi.fn());
    const button = find("button");
    expect(button?.textContent, "the button should say Add to cart").toBe("Add to cart");
    expect(button?.type, "the button should be type=\"button\"").toBe("button");
    expect(button?.disabled, "an item in stock should have a working button").toBe(false);
  });

  it("calls onAdd with the item id when Add to cart is clicked", () => {
    const onAdd = vi.fn();
    renderProduct(shelf(), golu, onAdd);
    find("button")?.click();
    expect(onAdd.mock.calls, "one click should call onAdd once, with DC-1042").toEqual([["DC-1042"]]);
  });

  it("disables Add to cart on a sold-out item", () => {
    renderProduct(shelf(), booties, vi.fn());
    expect(find(".stock")?.textContent, "the booties' stock line should say Sold out").toBe("Sold out");
    expect(find("button")?.disabled, "a sold-out item's button should be disabled").toBe(true);
  });

  it("never adds a sold-out item, even when its button is clicked", () => {
    const onAdd = vi.fn();
    renderProduct(shelf(), booties, onAdd);
    find("button")?.click();
    expect(onAdd.mock.calls.length, "clicking a sold-out item must not call onAdd").toBe(0);
  });

  it("replaces the old card when drawn again", () => {
    renderProduct(shelf(), golu, vi.fn());
    renderProduct(shelf(), coasters, vi.fn());
    expect(document.querySelectorAll("#shelf article").length, "drawing twice should leave one card").toBe(1);
    expect(find("h3")?.textContent, "the new card should show the coaster set").toBe("Coaster set (4)");
  });

  it("shows a name as text, never as HTML", () => {
    renderProduct(shelf(), { ...golu, name: "<b>Golu</b>" }, vi.fn());
    expect(find("h3")?.textContent, "the name should appear exactly as typed, tags and all").toBe("<b>Golu</b>");
    expect(find("b"), "the name must not turn into a real <b> element").toBeNull();
  });

  it("returns the card it drew", () => {
    const article = renderProduct(shelf(), golu, vi.fn());
    expect(article?.tagName, "renderProduct should return an <article> element").toBe("ARTICLE");
    expect(article, "renderProduct should return the very <article> it put on the shelf").toBe(find("article"));
  });
});
