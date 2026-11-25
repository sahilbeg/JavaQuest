import { describe, it, expect, beforeEach } from "vitest";
import { createOrder } from "../src/lib/orders.js";
import { resetDb, allOrders, findOrder, deleteOrder } from "../src/lib/db.js";
import { ASHA, RAVI, SEEDED, numberOf } from "./customers.js";

beforeEach(() => {
  resetDb();
});

describe("Silver: numbers are never reused", () => {
  it("never takes the number of an order that exists, even after another order was deleted", async () => {
    await deleteOrder("order-18");
    const order = await createOrder(ASHA);
    expect(SEEDED.includes(order.id), `the new order got ${order.id}, which already belongs to someone (counting orders breaks after a delete)`).toBe(false);
    expect((await findOrder("order-19"))?.name, "Neha Desai's order-19 must not be overwritten").toBe("Neha Desai");
    expect((await allOrders()).length, "2 old orders plus 1 new one should make 3").toBe(3);
  });

  it("hands out numbers that only go up", async () => {
    const first = await createOrder(ASHA);
    const second = await createOrder(RAVI);
    expect(numberOf(first.id) > 19, `${first.id} should come after the last old order, order-19`).toBe(true);
    expect(numberOf(second.id) > numberOf(first.id), `${second.id} should come after ${first.id}`).toBe(true);
  });

  it("gives ten orders placed at once ten different numbers", async () => {
    const orders = await Promise.all(Array.from({ length: 10 }, () => createOrder(ASHA)));
    expect(new Set(orders.map((o) => o.id)).size, "ten orders at the same moment need ten different numbers").toBe(10);
    expect((await allOrders()).length, "all ten should be saved next to the 3 old ones").toBe(13);
  });
});
