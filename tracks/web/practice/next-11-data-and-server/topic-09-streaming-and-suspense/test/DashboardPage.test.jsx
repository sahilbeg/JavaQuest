import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup, waitFor, act } from "@testing-library/react";

vi.mock("../src/lib/reports.js", () => ({ getTodaySales: vi.fn(), getLowStock: vi.fn() }));

import { getTodaySales, getLowStock } from "../src/lib/reports.js";
import DashboardPage from "../src/app/dashboard/page.jsx";

/** A promise the test finishes by hand: held.resolve(value). */
function held() {
  let resolve;
  const promise = new Promise((r) => {
    resolve = r;
  });
  return { promise, resolve };
}

let sales;
let stock;

beforeEach(() => {
  vi.clearAllMocks();
  sales = held();
  stock = held();
  getTodaySales.mockReturnValue(sales.promise);
  getLowStock.mockReturnValue(stock.promise);
});

afterEach(cleanup);

/** Renders the page, checking it returns at once instead of waiting for the slow reports. */
async function renderPage() {
  const result = await Promise.race([Promise.resolve(DashboardPage()), new Promise((r) => setTimeout(() => r("still waiting"), 50))]);
  expect(result, "DashboardPage should return straight away, without awaiting the reports (pass the promises down)").not.toBe("still waiting");
  let view;
  // The cards suspend on their promises; act lets React finish that work before the test looks.
  await act(async () => {
    view = render(result);
  });
  return view;
}

describe("DashboardPage", () => {
  it("starts both reports straight away", async () => {
    await renderPage();
    expect(getTodaySales.mock.calls.length, "the page should call getTodaySales() once").toBe(1);
    expect(getLowStock.mock.calls.length, "the page should call getLowStock() once").toBe(1);
  });

  it("shows the heading at once, with a loading message for each slow card", async () => {
    await renderPage();
    expect(screen.queryByRole("heading", { level: 1, name: "Today at Dhaaga" }), "the heading needs no data, so it should show at once").not.toBeNull();
    expect(screen.queryByText("Loading today's sales..."), "while the sales load, the sales boundary should show Loading today's sales...").not.toBeNull();
    expect(screen.queryByText("Loading low stock..."), "while the stock loads, the stock boundary should show Loading low stock...").not.toBeNull();
  });

  it("shows the stock as soon as it arrives, while the sales are still loading", async () => {
    await renderPage();
    await act(async () => {
      stock.resolve([{ id: "DC-1042", name: "Golu the elephant", left: 2 }]);
    });
    await waitFor(() => expect(screen.queryByText("Golu the elephant: 2 left"), "the stock card should fill in once its report arrives").not.toBeNull());
    expect(
      screen.queryByText("Loading today's sales..."),
      "the sales are still loading, so their message should stay (give each card its own Suspense)",
    ).not.toBeNull();
  });

  it("fills in the sales card when its report arrives", async () => {
    await renderPage();
    await act(async () => {
      sales.resolve({ orders: 12, totalPaise: 845000 });
    });
    await waitFor(() => expect(screen.queryByText("12 orders, ₹8,450.00"), "the sales card should fill in once its report arrives").not.toBeNull());
    expect(screen.queryByText("Loading low stock..."), "the stock is still loading, so its message should stay").not.toBeNull();
  });
});
