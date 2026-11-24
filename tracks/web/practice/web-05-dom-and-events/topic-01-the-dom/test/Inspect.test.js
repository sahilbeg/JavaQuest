import { describe, it, expect, beforeEach } from "vitest";
import { countProducts, productNames, firstAndLast, parentId } from "../src/inspect.js";

beforeEach(() => {
  document.body.innerHTML = `
    <section id="catalogue">
      <h2>Catalogue</h2>
      <ul id="products">
        <li>Golu the elephant</li>
        <li>
          Marigold coaster set
        </li>
        <li>Indigo tote bag</li>
      </ul>
    </section>
    <section id="wishlist">
      <ul id="empty"></ul>
    </section>`;
});

const products = () => document.getElementById("products");
const empty = () => document.getElementById("empty");

describe("countProducts", () => {
  it("counts the three products, not the spaces between them", () => {
    expect(countProducts(products()), "three <li> items should count as 3; the spaces between tags are text nodes, not products").toBe(3);
  });

  it("counts zero for an empty list", () => {
    expect(countProducts(empty()), "a list with no <li> should count as 0").toBe(0);
  });

  it("follows the page when a product is added", () => {
    products().insertAdjacentHTML("beforeend", "<li>Mint baby booties</li>");
    expect(countProducts(products()), "after adding a fourth <li>, the count should be 4").toBe(4);
  });
});

describe("productNames", () => {
  it("lists every name in page order, trimmed", () => {
    expect(productNames(products()), "the names should be trimmed and in the same order as the page").toEqual([
      "Golu the elephant",
      "Marigold coaster set",
      "Indigo tote bag",
    ]);
  });

  it("returns a real array", () => {
    expect(Array.isArray(productNames(products())), "productNames should return an array, not an HTMLCollection").toBe(true);
  });

  it("returns an empty array for an empty list", () => {
    expect(productNames(empty()), "an empty list has no names").toEqual([]);
  });
});

describe("firstAndLast", () => {
  it("finds the first and last product names", () => {
    expect(firstAndLast(products()), "first should be Golu the elephant and last should be Indigo tote bag").toEqual({
      first: "Golu the elephant",
      last: "Indigo tote bag",
    });
  });

  it("trims the names", () => {
    products().firstElementChild.remove();
    expect(firstAndLast(products())?.first, "the first name should have no line breaks or spaces around it").toBe("Marigold coaster set");
  });

  it("gives null for an empty list", () => {
    expect(firstAndLast(empty()), "an empty list has no first or last item, so the answer is null").toBe(null);
  });
});

describe("parentId", () => {
  it("names the section that holds the product list", () => {
    expect(parentId(products()), "the products list sits inside section#catalogue").toBe("catalogue");
  });

  it("works for any element, like the heading", () => {
    const heading = document.querySelector("h2");
    expect(parentId(heading), "the <h2> sits inside section#catalogue").toBe("catalogue");
  });

  it("walks up only one step", () => {
    const item = products().firstElementChild;
    expect(parentId(item), "an <li> sits inside ul#products, so its parent's id is products").toBe("products");
  });
});
