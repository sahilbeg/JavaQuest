import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import ShopPage, { metadata } from "../src/app/items/page.jsx";

// A stand-in for next/navigation, used by Filters inside the page.
vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
  useRouter: () => ({ push: vi.fn() }),
  usePathname: () => "/items",
}));

afterEach(cleanup);

/** Renders the page the way Next.js 16 calls it: searchParams is a Promise. */
async function open(search = {}) {
  return render(await ShopPage({ params: Promise.resolve({}), searchParams: Promise.resolve(search) }));
}

const rows = (container) => [...container.querySelectorAll("ul li")];

describe("ShopPage", () => {
  it("has the title Shop and a description", () => {
    expect(metadata.title, "the shop's metadata title should be Shop").toBe("Shop");
    expect(metadata.description, "the description should be the one in README.md").toBe("Every handmade piece from Dhaaga & Co., Pune.");
  });

  it("lists every piece when nothing is chosen", async () => {
    const { container } = await open();
    expect(screen.queryByRole("heading", { level: 1 })?.textContent, "the page should keep its <h1> Shop").toBe("Shop");
    expect(screen.queryByText("Showing 6 of 6 pieces"), "with no choices, it should say Showing 6 of 6 pieces").not.toBeNull();
    expect(rows(container).length, "the list should have 6 <li>s").toBe(6);
  });

  it("links each piece to its own page, with its price", async () => {
    await open();
    const golu = screen.queryByRole("link", { name: "Golu the elephant" });
    expect(golu?.getAttribute("href"), "Golu's name should link to /items/DC-1042").toBe("/items/DC-1042");
    expect(golu?.closest("li")?.textContent, "Golu's row should read Golu the elephant ₹450.00").toBe("Golu the elephant ₹450.00");
  });

  it("marks sold-out pieces", async () => {
    await open();
    const mint = screen.queryByRole("link", { name: "Mint keychain" })?.closest("li");
    expect(mint?.textContent.includes("Sold out"), "the Mint keychain has 0 left, so its row should say Sold out").toBe(true);
    const golu = screen.queryByRole("link", { name: "Golu the elephant" })?.closest("li");
    expect(golu?.textContent.includes("Sold out"), "Golu has 7 left, so his row should not say Sold out").toBe(false);
  });

  it("follows the choices in the address", async () => {
    const { container } = await open({ colour: "indigo", sort: "price-desc" });
    expect(screen.queryByText("Showing 2 of 6 pieces"), "?colour=indigo should say Showing 2 of 6 pieces").not.toBeNull();
    const names = rows(container).map((li) => li.querySelector("a")?.textContent);
    expect(names, "Indigo, dearest first: the tote bag, then Golu").toEqual(["Indigo tote bag", "Golu the elephant"]);
  });

  it("says so, with a way back, when nothing matches", async () => {
    const { container } = await open({ q: "dragon" });
    expect(screen.queryByText("Nothing matches."), "searching dragon should say Nothing matches.").not.toBeNull();
    expect(rows(container).length, "with nothing matching, there should be no list items").toBe(0);
    const back = screen.queryByRole("link", { name: "See everything" });
    expect(back?.getAttribute("href"), "a See everything link should go to /items").toBe("/items");
  });

  it("shows the filters with the current choices", async () => {
    await open({ colour: "mint", q: "key" });
    expect(screen.queryByLabelText("Colour")?.value, "the Colour menu should show the colour from the address").toBe("mint");
    expect(screen.queryByLabelText("Search")?.value, "the Search box should start with the search from the address").toBe("key");
  });
});
