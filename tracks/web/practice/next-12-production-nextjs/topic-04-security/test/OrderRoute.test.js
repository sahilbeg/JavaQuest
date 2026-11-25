import { describe, it, expect, beforeEach } from "vitest";
import { GET } from "../src/app/api/orders/[id]/route.js";
import { resetOrders } from "../src/lib/orders.js";

const ASHA = "session=s-7f3a9c";
const RAVI = "theme=dark; session=s-91bc04";

/** Asks the route handler for one order, with an optional Cookie header. */
async function ask(id, cookie) {
  const request = new Request(`http://localhost/api/orders/${id}`, { headers: cookie ? { cookie } : {} });
  const response = await GET(request, { params: Promise.resolve({ id }) });
  return { status: response.status, body: await response.json() };
}

beforeEach(resetOrders);

describe("GET /api/orders/[id]", () => {
  it("asks a visitor with no session to log in", async () => {
    expect(await ask("order-17"), "no session cookie should answer 401 with { error: \"Please log in\" } (TODO 3)").toEqual({
      status: 401,
      body: { error: "Please log in" },
    });
  });

  it("treats a made-up session token like no session", async () => {
    expect(await ask("order-17", "session=s-guess"), "an unknown session token should also answer 401 (TODO 3)").toEqual({
      status: 401,
      body: { error: "Please log in" },
    });
  });

  it("won't show Ravi an order that belongs to Asha", async () => {
    expect(await ask("order-17", RAVI), "someone else's order should answer the same 404 as a missing order, so nobody learns it exists (TODO 3)").toEqual({
      status: 404,
      body: { error: "Order not found" },
    });
  });

  it("says 404 for an order that doesn't exist", async () => {
    expect((await ask("order-99", ASHA)).status, "a missing order should answer 404").toBe(404);
  });

  it("shows Asha her own order, without her phone number or address", async () => {
    expect(await ask("order-17", ASHA), "Asha should get only id, itemId, qty, totalPaise and status (TODO 4)").toEqual({
      status: 200,
      body: { id: "order-17", itemId: "DC-1042", qty: 1, totalPaise: 45000, status: "PACKED" },
    });
  });
});
