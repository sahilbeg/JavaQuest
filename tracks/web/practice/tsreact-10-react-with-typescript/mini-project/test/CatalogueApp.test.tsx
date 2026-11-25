import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CatalogueApp } from "../src/CatalogueApp.tsx";
import { PRODUCTS } from "../src/products.ts";

afterEach(cleanup);

function setup() {
  const user = userEvent.setup();
  const { container } = render(<CatalogueApp products={PRODUCTS} />);
  return {
    user,
    container,
    names: () => [...container.querySelectorAll("section.grid article.product-card h3")].map((h) => h.textContent).join(" | "),
    showing: () => container.querySelector(".showing")?.textContent,
    cartCount: () => container.querySelector(".cart-count")?.textContent,
    search: () => screen.queryByLabelText("Search") as HTMLInputElement,
    chip: (name: string) => screen.queryByRole("button", { name }) as HTMLButtonElement,
    add: (name: string) => screen.queryByRole("button", { name: `Add ${name} to cart` }) as HTMLButtonElement,
  };
}

describe("CatalogueApp", () => {
  it("shows all six products and an empty cart at first", () => {
    const { container, showing, cartCount } = setup();
    expect(container.querySelectorAll("section.grid article.product-card").length, "all six products should have a card").toBe(6);
    expect(showing(), "it should say Showing 6 of 6 items").toBe("Showing 6 of 6 items");
    expect(cartCount(), "the cart should start at Cart (0)").toBe("Cart (0)");
  });

  it("finds products by name, ignoring capitals and spaces at the ends", async () => {
    const { user, search, names, showing } = setup();
    expect(search(), "there should be a search box labelled Search").not.toBeNull();
    await user.type(search(), "  GOLU ");
    expect(names(), "  GOLU  should find only Golu the elephant").toBe("Golu the elephant");
    expect(showing(), "it should say Showing 1 of 6 items").toBe("Showing 1 of 6 items");
  });

  it("shows only the chosen colour, and marks its button", async () => {
    const { user, chip, names } = setup();
    expect(chip("Indigo"), "there should be a colour button for Indigo").not.toBeNull();
    await user.click(chip("Indigo"));
    expect(names(), "Indigo should show Golu and the tote bag").toBe("Golu the elephant | Indigo tote bag");
    expect(chip("Indigo").getAttribute("aria-pressed"), "the Indigo button should be pressed").toBe("true");
    expect(chip("All").getAttribute("aria-pressed"), "All should no longer be pressed").toBe("false");
    await user.click(chip("All"));
    expect(names().split(" | ").length, "All should bring back every product").toBe(6);
  });

  it("uses the search and the colour together", async () => {
    const { user, search, chip, names } = setup();
    await user.type(search(), "coaster");
    await user.click(chip("Off-white"));
    expect(names(), "coaster plus Off-white should leave only the Off-white coaster set").toBe("Off-white coaster set");
  });

  it("says so when nothing matches", async () => {
    const { user, search, container, showing } = setup();
    await user.type(search(), "lantern");
    expect(container.querySelector(".empty")?.textContent, "with no match, it should say No items match your search.").toBe(
      "No items match your search.",
    );
    expect(container.querySelector("section.grid"), "the grid should not be shown when nothing matches").toBeNull();
    expect(showing(), "it should say Showing 0 of 6 items").toBe("Showing 0 of 6 items");
  });

  it("counts every Add, and filtering never resets the cart", async () => {
    const { user, add, chip, cartCount } = setup();
    await user.click(add("Marigold coaster set"));
    await user.click(add("Indigo tote bag"));
    await user.click(add("Indigo tote bag"));
    expect(cartCount(), "three Adds should make Cart (3)").toBe("Cart (3)");
    await user.click(chip("Mint"));
    await user.click(chip("All"));
    expect(cartCount(), "changing the colour should not empty the cart").toBe("Cart (3)");
  });

  it("never lets the cart hold more Golus than are in stock", async () => {
    const { user, add, cartCount } = setup();
    await user.click(add("Golu the elephant"));
    expect(add("Golu the elephant").disabled, "one Golu in the cart and two in stock: Add should still work").toBe(false);
    await user.click(add("Golu the elephant"));
    expect(cartCount(), "two Golus should make Cart (2)").toBe("Cart (2)");
    expect(add("Golu the elephant").disabled, "both Golus are in the cart, so Add should be disabled").toBe(true);
    expect(add("Mint keychain").disabled, "the sold-out keychain can never be added").toBe(true);
  });

  it("keys every card and colour button, so React logs no key warnings", async () => {
    const errors = vi.spyOn(console, "error").mockImplementation(() => {});
    try {
      const { user, chip, container } = setup();
      await user.click(chip("Indigo"));
      expect(container.querySelectorAll("article.product-card").length, "two Indigo cards should be drawn").toBe(2);
      const keyWarnings = errors.mock.calls.filter((args) => args.map(String).join(" ").includes("key"));
      expect(keyWarnings.length, "React warned about keys: key each card by product.id").toBe(0);
    } finally {
      errors.mockRestore();
    }
  });
});
