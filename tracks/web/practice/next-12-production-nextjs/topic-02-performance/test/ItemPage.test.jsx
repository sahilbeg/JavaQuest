import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";

// A pretend data layer: every call waits until the check answers it, so the check can see what started when.
const fake = vi.hoisted(() => ({ started: [], waiting: [], buttonProps: [] }));

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
}));

vi.mock("../src/lib/shop.js", () => {
  const slow = (name, value) => (id) => {
    fake.started.push(`${name}(${id})`);
    return new Promise((resolve) => fake.waiting.push(() => resolve(value)));
  };
  return {
    listItems: async () => [],
    getItem: slow("getItem", { id: "DC-1042", name: "Golu the elephant", paise: 45000, description: "A small crocheted elephant." }),
    getReviews: slow("getReviews", [
      { by: "Asha", stars: 5, text: "Softer than I hoped." },
      { by: "Imran", stars: 4, text: "Lovely, but small." },
    ]),
    getStock: slow("getStock", 7),
  };
});

// A pretend client button that remembers the props the page sends it.
vi.mock("../src/components/AddToCartButton.jsx", () => ({
  AddToCartButton: (props) => {
    fake.buttonProps.push(props);
    return null;
  },
}));

import ItemPage from "../src/app/items/[id]/page.jsx";

const tick = () => new Promise((resolve) => setTimeout(resolve, 0));

/** Answers every waiting call, a few rounds over, until the page has everything it asked for. */
async function finish(pending) {
  for (let round = 0; round < 6; round++) {
    fake.waiting.splice(0).forEach((answer) => answer());
    await tick();
  }
  return pending;
}

beforeEach(() => {
  fake.started.length = 0;
  fake.waiting.length = 0;
  fake.buttonProps.length = 0;
});

afterEach(cleanup);

describe("Golu's item page", () => {
  it("starts loading the item, the reviews and the stock at the same time", async () => {
    const pending = ItemPage({ params: Promise.resolve({ id: "DC-1042" }) });
    await tick();
    const startedTogether = [...fake.started].sort();
    await finish(pending);
    expect(
      startedTogether,
      "before any answer came back, all three calls should already have started. Awaiting them one by one makes customers wait three times as long: use Promise.all (TODO 2)"
    ).toEqual(["getItem(DC-1042)", "getReviews(DC-1042)", "getStock(DC-1042)"]);
  });

  it("still shows the name, the price and the reviews", async () => {
    render(await finish(ItemPage({ params: Promise.resolve({ id: "DC-1042" }) })));
    expect(screen.queryByRole("heading", { level: 1, name: "Golu the elephant" }), "the h1 should be Golu's name").not.toBeNull();
    expect(screen.queryByText("₹450.00"), "Golu costs 45000 paise, so the page should show ₹450.00").not.toBeNull();
    expect(screen.queryByRole("heading", { level: 2, name: "Reviews (2)" }), "the reviews heading should count Golu's 2 reviews").not.toBeNull();
  });

  it("sends the client button only the item's id and its stock", async () => {
    render(await finish(ItemPage({ params: Promise.resolve({ id: "DC-1042" }) })));
    expect(fake.buttonProps.length > 0, "the page should still render AddToCartButton").toBe(true);
    expect(
      fake.buttonProps.at(-1),
      "every prop of a client component is packed into the page and sent to the phone. Pass only id and stock, not the whole item or the reviews (TODO 3)"
    ).toEqual({ id: "DC-1042", stock: 7 });
  });
});
