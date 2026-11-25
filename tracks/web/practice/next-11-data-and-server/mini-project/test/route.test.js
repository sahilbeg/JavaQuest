import { describe, it, expect, beforeEach } from "vitest";
import { GET } from "../src/app/api/orders/[id]/route.js";
import { resetOrders, createOrder } from "../src/lib/orders.js";

beforeEach(() => {
  resetOrders();
});

function ask(id) {
  return GET(new Request(`http://localhost/api/orders/${id}`), { params: Promise.resolve({ id }) });
}

describe("GET /api/orders/[id]", () => {
  it("tells the bot an order's status and total", async () => {
    createOrder({ name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-1042", qty: 1 }] });
    const res = await ask("order-20");
    expect(res.status, "a known order should answer 200").toBe(200);
    expect(await res.json(), "the answer should be exactly { id, status, total }, with no name or phone").toEqual({
      id: "order-20",
      status: "NEW",
      total: "₹510.00",
    });
  });

  it("answers 404 for an order that doesn't exist", async () => {
    const res = await ask("order-99");
    expect(res.status, "an unknown order should answer 404").toBe(404);
    expect(await res.json(), "the body should name the order").toEqual({ error: "No order order-99." });
  });
});
