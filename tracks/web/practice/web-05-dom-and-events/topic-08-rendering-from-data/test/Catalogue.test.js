import { describe, it, expect, beforeEach } from "vitest";
import { productCard, renderCatalogue, countText } from "../src/catalogue.js";

const GOLU = { id: "DC-1042", name: "Golu the elephant", paise: 45000, colour: "Indigo", stock: 4 };
const COASTERS = { id: "DC-1001", name: "Marigold coaster set", paise: 15000, colour: "Marigold", stock: 12 };
const TOTE = { id: "DC-1007", name: "Indigo tote bag", paise: 89900, colour: "Indigo", stock: 0 };
const PRODUCTS = [GOLU, COASTERS, TOTE];

beforeEach(() => {
  document.body.innerHTML = `<ul id="products"><li>Old hand-typed card</li></ul>`;
});

const list = () => document.querySelector("#products");
const ids = () => [...list().children].map((li) => li.dataset.id);

describe("productCard", () => {
  it("makes an <li class=\"product\"> with the product's id", () => {
    const card = productCard(GOLU);
    expect(card?.tagName, "productCard should return an <li>").toBe("LI");
    expect(card?.classList.contains("product"), "the card should have the class product").toBe(true);
    expect(card?.dataset.id, "the card should have data-id=\"DC-1042\"").toBe("DC-1042");
  });

  it("shows the name and the price in rupees", () => {
    const card = productCard(GOLU);
    expect(card?.querySelector("h3")?.textContent, "the <h3> should say Golu the elephant").toBe("Golu the elephant");
    expect(card?.querySelector(".price")?.textContent, "45000 paise should show as ₹450.00").toBe("₹450.00");
  });

  it("has a working Add to cart button when the item is in stock", () => {
    const button = productCard(GOLU)?.querySelector("button.add");
    expect(button?.textContent, "the button should say Add to cart").toBe("Add to cart");
    expect(button?.getAttribute("type"), "the button should have type=\"button\"").toBe("button");
    expect(button?.disabled, "the button should work when 4 are in stock").toBe(false);
  });

  it("marks a sold-out item and switches its button off", () => {
    const card = productCard(TOTE);
    const button = card?.querySelector("button.add");
    expect(card?.classList.contains("sold-out"), "a stock of 0 should add the class sold-out").toBe(true);
    expect(button?.disabled, "a sold-out item's button should be disabled").toBe(true);
    expect(button?.textContent, "a sold-out item's button should say Sold out").toBe("Sold out");
  });

  it("keeps tags in a name as plain text", () => {
    const card = productCard({ ...GOLU, name: "Golu <i>XL</i>" });
    expect(card?.querySelector("h3")?.textContent, "the name should appear exactly as given").toBe("Golu <i>XL</i>");
    expect(card?.querySelector("i"), "no real <i> element should appear: use textContent").toBe(null);
  });

  it("returns a card that is not on the page yet", () => {
    const card = productCard(GOLU);
    expect(card !== null && card !== undefined && !card.isConnected, "productCard only builds the card; renderCatalogue puts it on the page").toBe(true);
  });
});

describe("renderCatalogue", () => {
  it("draws one card per product, in order, replacing what was there", () => {
    renderCatalogue(list(), PRODUCTS);
    expect(ids(), "the list should hold exactly the three products, in order, and no old card").toEqual(["DC-1042", "DC-1001", "DC-1007"]);
  });

  it("never doubles the list when it draws again", () => {
    renderCatalogue(list(), PRODUCTS);
    renderCatalogue(list(), PRODUCTS);
    expect(list().children.length, "drawing the same three products twice should still show 3 cards").toBe(3);
  });

  it("redraws a filtered list", () => {
    renderCatalogue(list(), PRODUCTS);
    renderCatalogue(list(), PRODUCTS.filter((product) => product.colour === "Indigo"));
    expect(ids(), "after filtering to Indigo, only Golu and the tote bag should show").toEqual(["DC-1042", "DC-1007"]);
  });

  it("shows a message when nothing matches", () => {
    renderCatalogue(list(), PRODUCTS);
    renderCatalogue(list(), []);
    expect(list().children.length, "with no products the list should hold exactly one line").toBe(1);
    const only = list().firstElementChild;
    expect(only?.classList.contains("empty"), "that line should have the class empty").toBe(true);
    expect(only?.textContent, "that line should say No items match.").toBe("No items match.");
  });

  it("removes the message when products come back", () => {
    renderCatalogue(list(), []);
    renderCatalogue(list(), [COASTERS]);
    expect(list().querySelector(".empty"), "the empty message should be gone once there is a product").toBe(null);
    expect(ids(), "only the coaster set should show").toEqual(["DC-1001"]);
  });
});

describe("countText", () => {
  it("says No items for 0", () => {
    expect(countText(0), "0 should give No items").toBe("No items");
  });

  it("uses the singular for 1", () => {
    expect(countText(1), "1 should give 1 item").toBe("1 item");
  });

  it("uses the plural for more", () => {
    expect(countText(5), "5 should give 5 items").toBe("5 items");
  });
});
