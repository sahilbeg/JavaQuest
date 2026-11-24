import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PRODUCTS } from "../src/catalogue.js";
import { filterProducts } from "../src/filters.js";
import { Catalogue } from "../src/Catalogue.jsx";

afterEach(cleanup);

const ids = (products) => products.map((product) => product.id);
const shownNames = () => screen.queryAllByRole("heading", { level: 3 }).map((heading) => heading.textContent);
const statusText = () => screen.queryByRole("status")?.textContent;

describe("Stage 2: filterProducts", () => {
  it("keeps everything with no search and category All", () => {
    expect(ids(filterProducts(PRODUCTS, { search: "", category: "All" })), "no filter should keep all six products").toEqual(ids(PRODUCTS));
  });

  it("finds words anywhere in the name, ignoring capitals and spaces around them", () => {
    expect(ids(filterProducts(PRODUCTS, { search: "coaster", category: "All" })), "\"coaster\" should find both coaster sets").toEqual(["DC-1101", "DC-1102"]);
    expect(ids(filterProducts(PRODUCTS, { search: "  GOLU ", category: "All" })), "\"  GOLU \" should still find Golu").toEqual(["DC-1042"]);
  });

  it("keeps only the chosen category", () => {
    expect(ids(filterProducts(PRODUCTS, { search: "", category: "Accessories" })), "Accessories are the keychain and the tote bag").toEqual(["DC-1201", "DC-1301"]);
  });

  it("needs both the search and the category to match", () => {
    expect(filterProducts(PRODUCTS, { search: "coaster", category: "Toys" }), "no coaster set is a toy").toEqual([]);
  });
});

describe("Stage 2: Catalogue", () => {
  it("shows every product and says how many", () => {
    render(<Catalogue products={PRODUCTS} onAdd={vi.fn()} />);
    expect(shownNames().length, "all six products should show at first").toBe(6);
    expect(statusText(), "the status line should say \"6 products\"").toBe("6 products");
  });

  it("filters as the customer types in Search", async () => {
    const user = userEvent.setup();
    render(<Catalogue products={PRODUCTS} onAdd={vi.fn()} />);
    await user.type(screen.getByLabelText("Search"), "coaster");
    expect(shownNames(), "typing \"coaster\" should leave the two coaster sets").toEqual(["Marigold coaster set", "Indigo coaster set"]);
    expect(statusText(), "the status line should say \"2 products\"").toBe("2 products");
  });

  it("filters by Category, with a singular count for one product", async () => {
    const user = userEvent.setup();
    render(<Catalogue products={PRODUCTS} onAdd={vi.fn()} />);
    await user.selectOptions(screen.getByLabelText("Category"), "Toys");
    expect(shownNames(), "the Toys category should show only Golu").toEqual(["Golu the elephant"]);
    expect(statusText(), "one product should say \"1 product\", not \"1 products\"").toBe("1 product");
  });

  it("adds a product with a button named after it", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<Catalogue products={PRODUCTS} onAdd={onAdd} />);
    const button = screen.queryByRole("button", { name: "Add Golu the elephant to cart" });
    expect(button, "each Add button needs aria-label=\"Add <name> to cart\"").not.toBeNull();
    await user.click(button);
    expect(onAdd.mock.calls.length, "one click should call onAdd once").toBe(1);
    expect(onAdd.mock.calls[0][0].id, "onAdd should get Golu's product").toBe("DC-1042");
  });
});
