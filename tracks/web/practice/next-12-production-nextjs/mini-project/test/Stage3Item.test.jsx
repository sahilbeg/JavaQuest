import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import ItemPage, { generateMetadata } from "../src/app/items/[id]/page.jsx";
import { GET } from "../src/app/api/stock/route.js";
import { resetStore } from "../src/lib/store.js";

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
  useRouter: () => ({ push: vi.fn() }),
  usePathname: () => "/",
}));

const GOLU_DESCRIPTION = "A small crocheted elephant in Indigo and Marigold, modelled on our Golu.";

function params(id) {
  return { params: Promise.resolve({ id }) };
}

async function stock(query) {
  const response = await GET(new Request("http://localhost/api/stock" + query));
  return { status: response.status, body: await response.json() };
}

beforeEach(resetStore);
afterEach(cleanup);

describe("Stage 3: the item page", () => {
  it("shows Golu's name, price, description and stock, with an Add to cart button", async () => {
    render(await ItemPage(params("DC-1042")));
    expect(screen.queryByRole("heading", { level: 1, name: "Golu the elephant" }), "the h1 should be Golu's name (TODO 4)").not.toBeNull();
    expect(screen.queryByText("₹450.00"), "the page should show ₹450.00 (TODO 4)").not.toBeNull();
    expect(screen.queryByText(GOLU_DESCRIPTION), "the page should show Golu's description (TODO 4)").not.toBeNull();
    expect(screen.queryByText("7 in stock"), "the page should say 7 in stock (TODO 4)").not.toBeNull();
    expect(screen.queryByRole("button", { name: "Add to cart" })?.disabled, "there should be a working Add to cart button (TODO 4)").toBe(false);
  });

  it("shows a sold-out item with a disabled button", async () => {
    render(await ItemPage(params("DC-1205")));
    expect(screen.queryByText("Out of stock"), "the keychain's page should say Out of stock (TODO 4)").not.toBeNull();
    expect(screen.queryByRole("button", { name: "Sold out" })?.disabled, "pass stock to AddToCartButton, which shows a disabled Sold out button at 0 (TODO 4)").toBe(true);
  });

  it("calls notFound for an item that doesn't exist", async () => {
    await expect(ItemPage(params("DC-9999")), "an unknown id should call notFound() (TODO 4)").rejects.toThrow("NEXT_NOT_FOUND");
  });

  it("gives each item page its own title, description and canonical address", async () => {
    expect(await generateMetadata(params("DC-1042")), "Golu's metadata (TODO 4)").toEqual({
      title: "Golu the elephant",
      description: GOLU_DESCRIPTION,
      alternates: { canonical: "/items/DC-1042" },
    });
    expect(await generateMetadata(params("DC-9999")), "an unknown item should be kept out of search results (TODO 4)").toEqual({
      title: "Item not found",
      robots: { index: false },
    });
  });
});

describe("Stage 3: GET /api/stock", () => {
  it("answers each item's live stock as a number", async () => {
    expect(await stock("?id=DC-1042"), "Golu has 7 in stock (TODO 5)").toEqual({ status: 200, body: { id: "DC-1042", stock: 7 } });
    expect(await stock("?id=DC-1205"), "the keychain has 0, which is still a 200 answer (TODO 5)").toEqual({ status: 200, body: { id: "DC-1205", stock: 0 } });
  });

  it("answers 400 without an id and 404 for an unknown one", async () => {
    expect(await stock(""), "no id should answer 400 (TODO 5)").toEqual({ status: 400, body: { error: "Missing id" } });
    expect(await stock("?id=DC-9999"), "an unknown id should answer 404 (TODO 5)").toEqual({ status: 404, body: { error: "No item DC-9999" } });
  });
});
