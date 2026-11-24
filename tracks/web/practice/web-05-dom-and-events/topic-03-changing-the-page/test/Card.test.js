import { describe, it, expect, beforeEach } from "vitest";
import { setName, setPrice, showStock } from "../src/card.js";

beforeEach(() => {
  document.body.innerHTML = `
    <article class="product" data-id="DC-1042">
      <h3>Item</h3>
      <p class="price"></p>
      <p class="stock"></p>
      <button class="add" type="button">Add to cart</button>
    </article>`;
});

const card = () => document.querySelector(".product");
const text = (selector) => card().querySelector(selector).textContent;
const button = () => card().querySelector(".add");

describe("setName", () => {
  it("writes the name into the heading", () => {
    setName(card(), "Golu the elephant");
    expect(text("h3"), "the <h3> should say Golu the elephant").toBe("Golu the elephant");
  });

  it("shows tags in a name as plain text instead of making elements", () => {
    setName(card(), "Golu <b>the</b> elephant");
    expect(text("h3"), "the <h3> should show the name exactly as given, angle brackets and all").toBe("Golu <b>the</b> elephant");
    expect(card().querySelector("h3 b"), "no real <b> element should appear: use textContent, not innerHTML").toBe(null);
  });

  it("labels the button for screen readers", () => {
    setName(card(), "Golu the elephant");
    expect(button().getAttribute("aria-label"), "the button's aria-label should be Add Golu the elephant to cart").toBe("Add Golu the elephant to cart");
  });
});

describe("setPrice", () => {
  it("shows 45000 paise as ₹450.00", () => {
    setPrice(card(), 45000);
    expect(text(".price"), "45000 paise should show as ₹450.00").toBe("₹450.00");
  });

  it("shows 89900 paise as ₹899.00", () => {
    setPrice(card(), 89900);
    expect(text(".price"), "89900 paise should show as ₹899.00").toBe("₹899.00");
  });

  it("keeps the paise in data-paise", () => {
    setPrice(card(), 45000);
    expect(card().dataset.paise, "data-paise should hold \"45000\"").toBe("45000");
  });
});

describe("showStock", () => {
  it("marks an item with no stock as sold out", () => {
    showStock(card(), 0);
    expect(text(".stock"), "a stock of 0 should say Sold out").toBe("Sold out");
    expect(card().classList.contains("sold-out"), "a stock of 0 should add the class sold-out").toBe(true);
    expect(button().disabled, "the Add to cart button should be disabled at 0").toBe(true);
  });

  it("marks two left as low", () => {
    showStock(card(), 2);
    expect(text(".stock"), "a stock of 2 should say Only 2 left").toBe("Only 2 left");
    expect(card().classList.contains("low"), "a stock of 2 should add the class low").toBe(true);
    expect(card().classList.contains("sold-out"), "a stock of 2 is not sold out").toBe(false);
    expect(button().disabled, "the button should work while stock is left").toBe(false);
  });

  it("counts three left as low too", () => {
    showStock(card(), 3);
    expect(text(".stock"), "a stock of 3 should say Only 3 left").toBe("Only 3 left");
    expect(card().classList.contains("low"), "3 or fewer counts as low").toBe(true);
  });

  it("shows plenty of stock as in stock, with neither class", () => {
    showStock(card(), 12);
    expect(text(".stock"), "a stock of 12 should say In stock").toBe("In stock");
    expect(card().classList.contains("low"), "12 is not low").toBe(false);
    expect(card().classList.contains("sold-out"), "12 is not sold out").toBe(false);
  });

  it("turns a sold-out card back to normal when stock arrives", () => {
    showStock(card(), 0);
    showStock(card(), 12);
    expect(card().classList.contains("sold-out"), "after new stock the class sold-out should be removed").toBe(false);
    expect(button().disabled, "after new stock the button should work again").toBe(false);
    expect(text(".stock"), "after new stock the text should say In stock").toBe("In stock");
  });

  it("keeps the card's product class", () => {
    showStock(card(), 0);
    expect(card().classList.contains("product"), "the card must keep its product class: use classList, not className").toBe(true);
  });

  it("stores the stock in data-stock", () => {
    showStock(card(), 2);
    expect(card().dataset.stock, "data-stock should hold \"2\"").toBe("2");
  });
});
