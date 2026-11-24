import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Shop, ProductCard } from "../src/Shop.jsx";

afterEach(cleanup);

const PRODUCTS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1043", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1045", name: "Indigo tote bag", paise: 89900 },
];

function summary(container) {
  return {
    count: container.querySelector(".cart-count")?.textContent,
    total: container.querySelector(".cart-total")?.textContent,
  };
}

/** The "In cart: n" text on the card with this product name. */
function inCartOn(container, name) {
  const card = [...container.querySelectorAll("article.product-card")].find((a) => a.querySelector("h3")?.textContent === name);
  return card?.querySelector(".in-cart")?.textContent;
}

async function add(user, name, times = 1) {
  for (let i = 0; i < times; i++) {
    await user.click(screen.getByRole("button", { name: `Add ${name} to cart` }));
  }
}

describe("ProductCard", () => {
  it("shows the inCart number it is given", () => {
    const { container } = render(<ProductCard product={PRODUCTS[0]} inCart={4} onAdd={vi.fn()} />);
    expect(container.querySelector(".in-cart")?.textContent, "with inCart={4}, the card should say In cart: 4").toBe("In cart: 4");
  });

  it("tells its parent when Add is pressed", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<ProductCard product={PRODUCTS[0]} inCart={0} onAdd={onAdd} />);
    await add(user, "Golu the elephant");
    expect(onAdd.mock.calls.length, "pressing Add should call onAdd once").toBe(1);
    expect(onAdd.mock.calls[0]?.[0], "onAdd should get the product's id, DC-1042").toBe("DC-1042");
  });
});

describe("Shop", () => {
  it("starts with an empty cart", () => {
    const { container } = render(<Shop products={PRODUCTS} />);
    expect(summary(container).count, "the summary should start at Items in cart: 0").toBe("Items in cart: 0");
    expect(summary(container).total, "the summary should start at Total: ₹0.00").toBe("Total: ₹0.00");
  });

  it("counts every Add in the cart summary", async () => {
    const user = userEvent.setup();
    const { container } = render(<Shop products={PRODUCTS} />);
    await add(user, "Golu the elephant", 2);
    await add(user, "Marigold coaster set");
    expect(summary(container).count, "two elephants and one coaster set make Items in cart: 3").toBe("Items in cart: 3");
  });

  it("adds up the total from each price and quantity", async () => {
    const user = userEvent.setup();
    const { container } = render(<Shop products={PRODUCTS} />);
    await add(user, "Golu the elephant", 2);
    await add(user, "Marigold coaster set");
    expect(summary(container).total, "2 × ₹450 + ₹150 should show Total: ₹1,050.00").toBe("Total: ₹1,050.00");
  });

  it("shows each card how many of it are in the cart", async () => {
    const user = userEvent.setup();
    const { container } = render(<Shop products={PRODUCTS} />);
    await add(user, "Golu the elephant", 2);
    await add(user, "Marigold coaster set");
    expect(inCartOn(container, "Golu the elephant"), "Golu's card should say In cart: 2").toBe("In cart: 2");
    expect(inCartOn(container, "Marigold coaster set"), "the coaster set's card should say In cart: 1").toBe("In cart: 1");
    expect(inCartOn(container, "Indigo tote bag"), "the tote bag was never added, so In cart: 0").toBe("In cart: 0");
  });

  it("empties the summary and every card with Clear cart", async () => {
    const user = userEvent.setup();
    const { container } = render(<Shop products={PRODUCTS} />);
    await add(user, "Golu the elephant");
    await add(user, "Indigo tote bag");
    const clear = screen.getByRole("button", { name: "Clear cart" });
    expect(clear.disabled, "with items in the cart, Clear cart should be enabled").toBe(false);
    await user.click(clear);
    expect(summary(container).count, "after Clear cart the summary should say Items in cart: 0").toBe("Items in cart: 0");
    expect(inCartOn(container, "Golu the elephant"), "after Clear cart, Golu's card should say In cart: 0").toBe("In cart: 0");
    expect(inCartOn(container, "Indigo tote bag"), "after Clear cart, the tote bag's card should say In cart: 0").toBe("In cart: 0");
  });
});
