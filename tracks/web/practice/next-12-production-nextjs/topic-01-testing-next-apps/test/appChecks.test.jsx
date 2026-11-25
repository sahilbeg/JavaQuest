import { describe, it, expect, afterEach, vi } from "vitest";
import { cleanup } from "@testing-library/react";
import { GET } from "../src/app/api/stock/route.js";
import ItemPage from "../src/app/items/[id]/page.jsx";
import { placeOrder } from "../src/app/actions.js";
import { resetOrders } from "../src/lib/orders.js";
import { checkStockFound, checkStockUnknown, checkItemPage, checkItemNotFound, checkPlaceOrder } from "../src/appChecks.jsx";
import { StockAsText, StockUnknownIsFine, PriceInPaise, ParamsNotAwaited, NoNotFound, AcceptsZero, IgnoresQuantity } from "./brokenApp.jsx";

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

const CHECKS = { checkStockFound, checkStockUnknown, checkItemPage, checkItemNotFound, checkPlaceOrder };
const REAL = { GET, ItemPage, placeOrder };

afterEach(cleanup);

/** Runs one of your checks on an app. Returns "passed", or "failed: <first line of the error>". */
async function outcome(check, app) {
  resetOrders();
  try {
    await check(app);
    return "passed";
  } catch (error) {
    return "failed: " + String(error?.message ?? error).split("\n")[0];
  } finally {
    cleanup();
  }
}

/** The names of your checks that fail on this app. */
async function checksThatFail(app) {
  const failing = [];
  for (const [name, check] of Object.entries(CHECKS)) {
    if ((await outcome(check, app)) !== "passed") failing.push(name);
  }
  return failing;
}

describe("Your Next.js checks", () => {
  it("all pass on the real, working app", async () => {
    for (const [name, check] of Object.entries(CHECKS)) {
      expect(await outcome(check, REAL), `${name} should pass on the real app. A check that fails on working code is wrong`).toBe("passed");
    }
  });

  it("catch a stock answer that sends the number as text", async () => {
    expect((await checksThatFail({ ...REAL, GET: StockAsText })).length > 0, "no check failed when stock came back as \"7\". The worked example checkStockFound should catch this").toBe(true);
  });

  it("catch a stock answer that says 200 for an item that doesn't exist", async () => {
    expect((await checksThatFail({ ...REAL, GET: StockUnknownIsFine })).length > 0, "no check failed when DC-9999 got 200 OK. Ask for DC-9999 and expect 404 with { error: \"No item DC-9999\" } (TODO 1)").toBe(true);
  });

  it("catch an item page that shows paise as rupees", async () => {
    expect((await checksThatFail({ ...REAL, ItemPage: PriceInPaise })).length > 0, "no check failed when Golu showed as ₹45000.00. Render the page and look for ₹450.00 (TODO 2)").toBe(true);
  });

  it("catch an item page that forgets to await params", async () => {
    expect((await checksThatFail({ ...REAL, ItemPage: ParamsNotAwaited })).length > 0, "no check failed when the page read params.id without await. Pass params as Promise.resolve({ id: \"DC-1042\" }), as Next.js 16 does (TODO 2)").toBe(true);
  });

  it("catch an item page that never calls notFound", async () => {
    expect((await checksThatFail({ ...REAL, ItemPage: NoNotFound })).length > 0, "no check failed when DC-9999 showed a normal page. Expect ItemPage to reject with NEXT_NOT_FOUND (TODO 3)").toBe(true);
  });

  it("catch an order action that accepts a quantity of 0", async () => {
    expect((await checksThatFail({ ...REAL, placeOrder: AcceptsZero })).length > 0, "no check failed when qty \"0\" made an order. Send qty \"0\" and expect the error (TODO 4)").toBe(true);
  });

  it("catch an order action that ignores the quantity", async () => {
    expect((await checksThatFail({ ...REAL, placeOrder: IgnoresQuantity })).length > 0, "no check failed when two Golus cost 45000 paise. Send qty \"2\" and expect totalPaise 90000 (TODO 4)").toBe(true);
  });
});
