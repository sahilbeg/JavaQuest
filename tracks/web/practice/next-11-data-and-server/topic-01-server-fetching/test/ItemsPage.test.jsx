import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import ItemsPage from "../src/app/items/page.jsx";
import { stubApi, heldFetch, reply, ITEMS, STOCK, ITEMS_URL, STOCK_URL } from "./fakeApi.js";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("ItemsPage", () => {
  it("arrives with one line per item, showing its price and stock", async () => {
    stubApi();
    render(await ItemsPage());
    expect(screen.queryByRole("heading", { level: 1, name: "Catalogue" }), "the page should keep its <h1>Catalogue</h1>").not.toBeNull();
    expect(screen.queryAllByRole("listitem").length, "two items from the API should make two <li>").toBe(2);
    expect(
      screen.queryByText("Golu the elephant: ₹450.00 (7 left)"),
      "Golu costs 45000 paise and has 7 left, so the line should read Golu the elephant: ₹450.00 (7 left)",
    ).not.toBeNull();
  });

  it("says Sold out for an item with none left", async () => {
    stubApi();
    render(await ItemsPage());
    expect(screen.queryByText("Coaster set: ₹150.00 (Sold out)"), "the coaster set has 0 left, so it should say (Sold out)").not.toBeNull();
  });

  it("says Sold out for an item the stock list doesn't mention", async () => {
    stubApi({ [STOCK_URL]: [{ "DC-1042": 7 }, 200] });
    render(await ItemsPage());
    expect(
      screen.queryByText("Coaster set: ₹150.00 (Sold out)"),
      "an item missing from the stock answer should count as sold out (try stock[item.id] ?? 0)",
    ).not.toBeNull();
  });

  it("shows a friendly message when the catalogue is empty", async () => {
    stubApi({ [ITEMS_URL]: [[], 200] });
    render(await ItemsPage());
    expect(screen.queryByText("No items yet. Check back soon!"), "with no items, the page should say No items yet. Check back soon!").not.toBeNull();
    expect(screen.queryByRole("list"), "with no items, there should be no empty <ul>").toBeNull();
  });

  it("asks for the items and the stock at the same time", async () => {
    const { fetchMock, waiting } = heldFetch();
    const page = ItemsPage();
    await Promise.resolve();
    const asked = fetchMock.mock.calls.map((call) => String(call[0])).sort();
    expect(asked, "both requests should leave before either answer arrives (use Promise.all)").toEqual([ITEMS_URL, STOCK_URL]);

    waiting[ITEMS_URL](reply(ITEMS));
    waiting[STOCK_URL](reply(STOCK));
    render(await page);
    expect(screen.queryByText("Golu the elephant: ₹450.00 (7 left)"), "once both answers arrive, the page should show the items").not.toBeNull();
  });

  it("lets the error page take over when the API fails", async () => {
    stubApi({ [ITEMS_URL]: [{ message: "down" }, 503] });
    await expect(ItemsPage(), "when the items API fails, ItemsPage should throw, so error.jsx shows").rejects.toThrow("Items API answered 503");
  });
});
