import { describe, it, expect } from "vitest";
import { page } from "./html.js";

const IDS = ["DC-1042", "DC-1101", "DC-1201", "DC-1301"];

describe("the page without JavaScript: search", () => {
  it("searches with a real form that sends to /search with get", () => {
    const form = page().querySelector("#search-form");
    expect(form?.tagName, "#search-form should be a <form>, not a <div>").toBe("FORM");
    expect(form?.getAttribute("action"), "the search form should send to /search").toBe("/search");
    expect(form?.getAttribute("method")?.toLowerCase(), "a search only reads, so its method should be get").toBe("get");
  });

  it("sends the search words under the name q, from a labelled box", () => {
    const form = page().querySelector("#search-form");
    const input = form?.querySelector("#q");
    expect(input?.getAttribute("name"), "the search box should have name=\"q\", or its words are never sent").toBe("q");
    expect(page().querySelector('label[for="q"]'), "the search box should keep its <label for=\"q\">").not.toBeNull();
    expect(form?.querySelector('button[type="submit"]'), "the search form needs a <button type=\"submit\">").not.toBeNull();
  });
});

describe("the page without JavaScript: products", () => {
  it("links every product name to its own page", () => {
    const hrefs = [...page().querySelectorAll(".product h2 a")].map((link) => link.getAttribute("href"));
    expect(hrefs, "each product's <h2> should hold a link to /products/<id>").toEqual(IDS.map((id) => `/products/${id}`));
  });

  it("adds to the cart with a form that posts to /cart", () => {
    const cards = [...page().querySelectorAll(".product")];
    expect(cards.length, "there should still be four products").toBe(4);
    cards.forEach((card, index) => {
      const form = card.querySelector("form.add");
      expect(form, `product ${IDS[index]} should have a <form class="add">`).not.toBeNull();
      expect(form?.getAttribute("action"), `product ${IDS[index]}'s form should send to /cart`).toBe("/cart");
      expect(form?.getAttribute("method")?.toLowerCase(), `adding to the cart changes something, so ${IDS[index]}'s form should use post`).toBe("post");
    });
  });

  it("sends which item to add in a hidden field called id", () => {
    const ids = [...page().querySelectorAll('.product form.add input[type="hidden"][name="id"]')].map((input) => input.getAttribute("value"));
    expect(ids, "each form should hold <input type=\"hidden\" name=\"id\" value=\"...\"> with its product's id").toEqual(IDS);
  });

  it("has no Add to cart buttons that only work with JavaScript", () => {
    const products = page().querySelector("#products");
    expect(products?.querySelectorAll('button[type="button"]').length, "type=\"button\" buttons do nothing without a script; use submit buttons inside the forms").toBe(0);
    expect(products?.querySelectorAll('form.add button[type="submit"]').length, "each of the four forms should have a <button type=\"submit\">").toBe(4);
  });
});
