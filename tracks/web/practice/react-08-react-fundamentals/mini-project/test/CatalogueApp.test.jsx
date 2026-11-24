import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CatalogueApp } from "../src/CatalogueApp.jsx";
import { PRODUCTS } from "../src/products.js";

afterEach(cleanup);

function names(container) {
  return [...container.querySelectorAll("article.product-card h3")].map((h3) => h3.textContent).join(", ");
}

function text(container, selector) {
  return container.querySelector(selector)?.textContent;
}

/** The button with this accessible name, with a friendly failure when it is missing. */
function button(name) {
  const found = screen.queryByRole("button", { name });
  expect(found, `there should be a button labelled ${name}`).not.toBeNull();
  return found;
}

async function search(user, words) {
  const box = screen.queryByLabelText("Search");
  expect(box, "there should be a search box labelled Search").not.toBeNull();
  await user.clear(box);
  await user.type(box, words);
}

describe("The catalogue at first", () => {
  it("shows all six products", () => {
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    expect(container.querySelectorAll("article.product-card").length, "all six products should show before any search").toBe(6);
    expect(text(container, "p.showing"), "the count line should say Showing 6 of 6 items").toBe("Showing 6 of 6 items");
  });

  it("starts with an empty cart and All chosen", () => {
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    expect(text(container, "p.cart-count"), "the cart should start at Cart (0)").toBe("Cart (0)");
    const all = button("All");
    expect(all.getAttribute("aria-pressed"), "All should start chosen, with aria-pressed=\"true\"").toBe("true");
    expect(all.className, "the chosen button should have className \"chip chip--selected\"").toBe("chip chip--selected");
  });

  it("has a colour button for All and each of the five colours, in order", () => {
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    const labels = [...container.querySelectorAll(".colour-filter button")].map((b) => b.textContent);
    expect(labels.join(", "), "the buttons should be All, then the five colours").toBe("All, Marigold, Indigo, Rani pink, Mint, Off-white");
  });
});

describe("Searching", () => {
  it("shows only products whose name contains the search text", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await search(user, "coaster");
    expect(names(container), "coaster should find the two coaster sets").toBe("Marigold coaster set, Off-white coaster set");
    expect(text(container, "p.showing"), "the count line should say Showing 2 of 6 items").toBe("Showing 2 of 6 items");
  });

  it("ignores capitals and spaces at the ends", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await search(user, "  GOLU ");
    expect(names(container), "'  GOLU ' should still find Golu the elephant").toBe("Golu the elephant");
  });

  it("says so when nothing matches", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await search(user, "unicorn");
    expect(container.querySelectorAll("article.product-card").length, "no card should show for unicorn").toBe(0);
    expect(text(container, "p.empty"), "the grid should say No items match your search.").toBe("No items match your search.");
    expect(text(container, "p.showing"), "the count line should say Showing 0 of 6 items").toBe("Showing 0 of 6 items");
  });
});

describe("Choosing a colour", () => {
  it("shows only that colour and marks its button as chosen", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await user.click(button("Indigo"));
    expect(names(container), "Indigo should show Golu and the tote bag").toBe("Golu the elephant, Indigo tote bag");
    const indigo = button("Indigo");
    expect(indigo.getAttribute("aria-pressed"), "the Indigo button should now have aria-pressed=\"true\"").toBe("true");
    expect(indigo.className, "the Indigo button should now have className \"chip chip--selected\"").toBe("chip chip--selected");
    const all = button("All");
    expect(all.getAttribute("aria-pressed"), "All should no longer be chosen").toBe("false");
    expect(all.className, "an unchosen button should have className \"chip\"").toBe("chip");
  });

  it("works together with the search", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await search(user, "coaster");
    await user.click(button("Marigold"));
    expect(names(container), "coaster in Marigold should leave only the Marigold coaster set").toBe("Marigold coaster set");
    expect(text(container, "p.showing"), "the count line should say Showing 1 of 6 items").toBe("Showing 1 of 6 items");
  });

  it("shows everything again with All", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await user.click(button("Mint"));
    expect(names(container), "Mint should show only the Mint keychain").toBe("Mint keychain");
    await user.click(button("All"));
    expect(container.querySelectorAll("article.product-card").length, "All should bring back all six products").toBe(6);
  });
});

describe("The cart count", () => {
  it("goes up by one for every Add", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await user.click(button("Add Golu the elephant to cart"));
    await user.click(button("Add Golu the elephant to cart"));
    await user.click(button("Add Rani pink baby booties to cart"));
    expect(text(container, "p.cart-count"), "three presses of Add should show Cart (3)").toBe("Cart (3)");
  });

  it("is not reset by searching or choosing a colour", async () => {
    const user = userEvent.setup();
    const { container } = render(<CatalogueApp products={PRODUCTS} />);
    await user.click(button("Add Golu the elephant to cart"));
    await search(user, "mint");
    await user.click(button("Mint"));
    expect(text(container, "p.cart-count"), "filtering must not empty the cart: still Cart (1)").toBe("Cart (1)");
    await user.click(button("Add Mint keychain to cart"));
    expect(text(container, "p.cart-count"), "adding the keychain should make it Cart (2)").toBe("Cart (2)");
  });
});
