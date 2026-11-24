import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { ProductList, GiftsUnder } from "../src/Catalogue.jsx";

afterEach(cleanup);

/** A fresh copy of the test products for every test, so one test can never change another's data. */
function products() {
  return [
    { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
    { id: "DC-1045", name: "Indigo tote bag", paise: 89900 },
    { id: "DC-1043", name: "Marigold coaster set", paise: 15000 },
    { id: "DC-1044", name: "Mint keychain", paise: 12000 },
  ];
}

function texts(container, selector) {
  return [...container.querySelectorAll(selector)].map((element) => element.textContent);
}

describe("ProductList", () => {
  it("draws one list item per product", () => {
    const { container } = render(<ProductList products={products()} />);
    expect(container.querySelectorAll("ul.product-list li").length, "four products should make four <li> rows").toBe(4);
  });

  it("shows each product's name and price, in the order given", () => {
    const { container } = render(<ProductList products={products()} />);
    expect(texts(container, "li span.name").join(", "), "each row's span.name should hold the product's name").toBe(
      "Golu the elephant, Indigo tote bag, Marigold coaster set, Mint keychain",
    );
    expect(texts(container, "li span.price").join(", "), "each row's span.price should hold rupees(paise)").toBe(
      "₹450.00, ₹899.00, ₹150.00, ₹120.00",
    );
  });

  it("draws the list from the data, so a new product gets a row too", () => {
    const list = [...products(), { id: "DC-1046", name: "Rani pink baby booties", paise: 35000 }];
    const { container } = render(<ProductList products={list} />);
    expect(texts(container, "li span.name").at(-1), "the last row should be the new baby booties").toBe("Rani pink baby booties");
  });

  it("says Nothing here yet. when there are no products", () => {
    const { container } = render(<ProductList products={[]} />);
    expect(screen.queryByText("Nothing here yet."), "an empty list should show Nothing here yet.").not.toBeNull();
    expect(container.querySelector("p.empty"), "the message should be in <p className=\"empty\">").not.toBeNull();
    expect(container.querySelectorAll("li").length, "an empty list should have no rows").toBe(0);
  });
});

describe("GiftsUnder", () => {
  it("names the price limit in the heading", () => {
    render(<GiftsUnder products={products()} maxPaise={50000} />);
    expect(screen.queryByRole("heading", { level: 2 })?.textContent, "the heading should say Gifts under ₹500.00").toBe(
      "Gifts under ₹500.00",
    );
  });

  it("shows only the gifts under the limit, cheapest first", () => {
    const { container } = render(<GiftsUnder products={products()} maxPaise={50000} />);
    expect(texts(container, "li span.name").join(", "), "under ₹500: keychain, coaster set, then Golu").toBe(
      "Mint keychain, Marigold coaster set, Golu the elephant",
    );
  });

  it("leaves out an item that costs exactly the limit", () => {
    const { container } = render(<GiftsUnder products={products()} maxPaise={45000} />);
    expect(texts(container, "li span.name").join(", "), "Golu costs exactly ₹450, which is not under ₹450").toBe(
      "Mint keychain, Marigold coaster set",
    );
  });

  it("shows Nothing here yet. when no gift is cheap enough", () => {
    render(<GiftsUnder products={products()} maxPaise={10000} />);
    expect(screen.queryByText("Nothing here yet."), "nothing costs under ₹100, so the empty message should show").not.toBeNull();
  });

  it("does not change the order of the products it was given", () => {
    const list = products();
    render(<GiftsUnder products={list} maxPaise={50000} />);
    expect(list.map((p) => p.id).join(", "), "sort a copy: the products prop must keep its order").toBe(
      "DC-1042, DC-1045, DC-1043, DC-1044",
    );
  });
});
