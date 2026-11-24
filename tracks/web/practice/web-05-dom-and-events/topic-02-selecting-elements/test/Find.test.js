import { describe, it, expect, beforeEach } from "vitest";
import { productCards, productTitles, findProduct, priceOf, soldOutTitles } from "../src/find.js";

beforeEach(() => {
  document.body.innerHTML = `
    <header>
      <h1>Dhaaga &amp; Co.</h1>
      <p class="price">All prices in rupees</p>
    </header>
    <ul id="products">
      <li class="product" data-id="DC-1042">
        <h3>Golu the elephant</h3>
        <p class="price">₹450.00</p>
      </li>
      <li class="product sold-out" data-id="DC-1007">
        <h3>Indigo tote bag</h3>
        <p class="price">₹899.00</p>
      </li>
      <li class="product" data-id="DC-1013">
        <h3>Rani pink keychain</h3>
        <p class="price"> ₹120.00 </p>
      </li>
    </ul>
    <aside id="cart">
      <h3>Your cart</h3>
      <p class="sold-out">Nothing sold out in your cart</p>
    </aside>`;
});

describe("productCards", () => {
  it("finds all three product cards as an array", () => {
    const cards = productCards(document);
    expect(Array.isArray(cards), "productCards should return a real array, not a NodeList").toBe(true);
    expect(cards.length, "the page has three .product cards").toBe(3);
  });

  it("searches only inside the root it is given", () => {
    const aside = document.querySelector("#cart");
    expect(productCards(aside).length, "the cart <aside> holds no .product cards").toBe(0);
  });
});

describe("productTitles", () => {
  it("lists the product names in page order, without the cart heading", () => {
    expect(productTitles(document), "only <h3> elements inside a .product are product names").toEqual([
      "Golu the elephant",
      "Indigo tote bag",
      "Rani pink keychain",
    ]);
  });
});

describe("findProduct", () => {
  it("finds the card for DC-1013", () => {
    const card = findProduct(document, "DC-1013");
    expect(card === null ? null : card.querySelector("h3").textContent, "DC-1013 is the Rani pink keychain's card").toBe("Rani pink keychain");
  });

  it("gives null for an id that is not on the page", () => {
    expect(findProduct(document, "DC-9999"), "there is no DC-9999, so the answer is null").toBe(null);
  });
});

describe("priceOf", () => {
  it("reads the keychain's own price, not the first price on the page", () => {
    expect(priceOf(document, "DC-1013"), "DC-1013 costs ₹120.00; the header's note is not a price").toBe("₹120.00");
  });

  it("reads Golu's price", () => {
    expect(priceOf(document, "DC-1042"), "DC-1042 costs ₹450.00").toBe("₹450.00");
  });

  it("reads the tote bag's price", () => {
    expect(priceOf(document, "DC-1007"), "DC-1007 costs ₹899.00").toBe("₹899.00");
  });

  it("gives an empty string for a missing product instead of crashing", () => {
    expect(priceOf(document, "DC-9999"), "there is no DC-9999, so the price should be \"\"").toBe("");
  });
});

describe("soldOutTitles", () => {
  it("names only the products marked sold-out", () => {
    expect(soldOutTitles(document), "only the Indigo tote bag has both product and sold-out").toEqual(["Indigo tote bag"]);
  });

  it("follows the classes on the page", () => {
    document.querySelector('[data-id="DC-1042"]').classList.add("sold-out");
    expect(soldOutTitles(document), "after Golu is marked sold-out, both Golu and the tote bag are listed").toEqual([
      "Golu the elephant",
      "Indigo tote bag",
    ]);
  });
});
