import { describe, it, expect, beforeEach, vi } from "vitest";

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

import { placeOrder } from "../src/app/checkout/actions.js";
import { resetDb, allOrders, findOrder } from "../src/lib/db.js";

beforeEach(() => {
  resetDb();
});

/** The checkout form's FormData, with its hidden requestId. */
function form(requestId, name = "Asha Rao") {
  const data = new FormData();
  data.append("name", name);
  data.append("phone", "98765 43210");
  data.append("itemId", "DC-1042");
  data.append("qty", "1");
  data.append("requestId", requestId);
  return data;
}

/** Where placeOrder sent the customer: the redirect's message, like "NEXT_REDIRECT /orders/order-20". */
async function whereTo(promise) {
  try {
    await promise;
    return "no redirect";
  } catch (error) {
    return error.message;
  }
}

describe("Gold: the same form sent twice", () => {
  it("makes one order when the same form is sent twice at the same moment", async () => {
    const [first, second] = await Promise.all([whereTo(placeOrder(null, form("req-double-tap"))), whereTo(placeOrder(null, form("req-double-tap")))]);
    expect(first, "placeOrder should redirect to the new order's page").toMatch(/^NEXT_REDIRECT \/orders\/order-\d+$/);
    expect(second, "both taps should go to the same order's page").toBe(first);
    expect((await allOrders()).length, "a double tap must make one order, not two").toBe(4);
  });

  it("sends the customer to the same order when the form is sent again later", async () => {
    const first = await whereTo(placeOrder(null, form("req-sent-again")));
    const again = await whereTo(placeOrder(null, form("req-sent-again")));
    expect(again, "sending the same form again should go to the first order").toBe(first);
    expect((await allOrders()).length, "sending the same form again must not make a second order").toBe(4);
  });

  it("still makes separate orders for different customers at the same moment", async () => {
    const [asha, ravi] = await Promise.all([
      whereTo(placeOrder(null, form("req-asha", "Asha Rao"))),
      whereTo(placeOrder(null, form("req-ravi", "Ravi Joshi")))
    ]);
    expect(asha === ravi, "two different forms must make two different orders").toBe(false);
    expect((await allOrders()).length, "two customers should make two new orders").toBe(5);
    const ravisId = ravi.replace("NEXT_REDIRECT /orders/", "");
    expect((await findOrder(ravisId))?.name, "Ravi's page should show Ravi's order").toBe("Ravi Joshi");
  });
});
