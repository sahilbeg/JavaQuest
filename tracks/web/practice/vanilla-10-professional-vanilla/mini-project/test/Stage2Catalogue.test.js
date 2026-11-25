import { describe, it, expect, beforeEach, vi } from "vitest";
import { filterProducts } from "../src/filters.js";
import { mountCatalogue } from "../src/catalogueView.js";
import { PRODUCTS } from "../src/catalogue.js";

const names = (products) => products.map((product) => product.name);

describe("Stage 2: filterProducts", () => {
  it("finds names that contain the search, whatever the capitals", () => {
    expect(names(filterProducts(PRODUCTS, { search: "COASTER", category: "All" })), "COASTER should find both coaster sets").toEqual(["Indigo coaster set", "Marigold coaster set"]);
  });

  it("ignores spaces around the search", () => {
    expect(names(filterProducts(PRODUCTS, { search: "  golu ", category: "All" })), "'  golu ' should find Golu").toEqual(["Golu the elephant"]);
  });

  it("filters by category", () => {
    expect(names(filterProducts(PRODUCTS, { search: "", category: "Accessories" })), "Accessories holds the keychain and the tote bag").toEqual(["Rani pink keychain", "Mint tote bag"]);
  });

  it("needs both the search and the category to match", () => {
    expect(filterProducts(PRODUCTS, { search: "pink", category: "Home" }), "the Rani pink keychain is not in Home, so nothing matches").toEqual([]);
  });
});

describe("Stage 2: the catalogue", () => {
  let section;
  let onAdd;

  beforeEach(() => {
    document.body.innerHTML = `<section id="shop"></section>`;
    section = document.querySelector("#shop");
    onAdd = vi.fn();
    mountCatalogue(section, PRODUCTS, onAdd);
  });

  const shownNames = () => [...section.querySelectorAll(".product h3")].map((h3) => h3.textContent);
  const count = () => section.querySelector('[role="status"]')?.textContent;

  function type(text) {
    const search = section.querySelector("#search");
    search.value = text;
    search.dispatchEvent(new Event("input", { bubbles: true }));
  }

  function pick(category) {
    const select = section.querySelector("#category");
    select.value = category;
    select.dispatchEvent(new Event("change", { bubbles: true }));
  }

  it("labels the search box and the category filter", () => {
    expect(section.querySelector('label[for="search"]')?.textContent, "the search box should be labelled Search").toBe("Search");
    expect(section.querySelector('label[for="category"]')?.textContent, "the select should be labelled Category").toBe("Category");
  });

  it("shows every product at first, and says how many", () => {
    expect(shownNames().length, "all six products should show at first").toBe(6);
    expect(count(), "the status line should say 6 products").toBe("6 products");
  });

  it("filters as the customer types", () => {
    type("coaster");
    expect(shownNames(), "typing coaster should leave the two coaster sets").toEqual(["Indigo coaster set", "Marigold coaster set"]);
    expect(count(), "the status line should say 2 products").toBe("2 products");
  });

  it("filters by category, and says 1 product for one", () => {
    pick("Baby");
    expect(shownNames(), "Baby should leave only the booties").toEqual(["Off-white baby booties"]);
    expect(count(), "one product should say 1 product, not 1 products").toBe("1 product");
  });

  it("combines the search and the category", () => {
    pick("Accessories");
    type("mint");
    expect(shownNames(), "Accessories and mint should leave the Mint tote bag").toEqual(["Mint tote bag"]);
  });

  it("says so when nothing matches", () => {
    type("zebra");
    expect(shownNames().length, "no product is called zebra").toBe(0);
    expect(count(), "the status line should say 0 products").toBe("0 products");
  });

  it("names each Add button after its product", () => {
    const labels = [...section.querySelectorAll("button.add")].map((button) => button.getAttribute("aria-label"));
    expect(labels[0], "Golu's button should be named Add Golu the elephant to cart").toBe("Add Golu the elephant to cart");
    expect(labels.length, "there should be six named Add buttons").toBe(6);
  });

  it("adds the right product when its button is clicked", () => {
    section.querySelector('.product[data-id="DC-1310"] button.add')?.click();
    expect(onAdd.mock.calls, "clicking the tote bag's button should call onAdd once, with the tote bag").toEqual([[PRODUCTS[4]]]);
  });
});
