import { describe, it, expect } from "vitest";
import { ordersReducer } from "../src/orders.ts";

/** A fresh board each time, so one test can't change another's orders. */
function board() {
  return [
    { id: "order-17", customer: "Mrs. Kulkarni", status: "new" },
    { id: "order-18", customer: "Asha Rao", status: "packed" },
    { id: "order-19", customer: "Vikram Nair", status: "shipped" },
  ];
}

function statusOf(orders, id) {
  return orders.find((o) => o.id === id)?.status;
}

describe("ordersReducer", () => {
  it("adds a new order at the end with the status new", () => {
    const next = ordersReducer(board(), { type: "added", id: "order-20", customer: "Farah Khan" });
    expect(next.length, "adding an order should make four orders").toBe(4);
    expect(next[3], "the new order should be last, with the status new").toEqual({ id: "order-20", customer: "Farah Khan", status: "new" });
  });

  it("doesn't add the same order twice", () => {
    const orders = board();
    const next = ordersReducer(orders, { type: "added", id: "order-17", customer: "Mrs. Kulkarni" });
    expect(next, "order-17 is already on the board, so the same array should come back").toBe(orders);
  });

  it("packs a new order", () => {
    const next = ordersReducer(board(), { type: "packed", id: "order-17" });
    expect(statusOf(next, "order-17"), "packing a new order should make it packed").toBe("packed");
    expect(statusOf(next, "order-18"), "other orders should not change").toBe("packed");
  });

  it("ships a packed order", () => {
    const next = ordersReducer(board(), { type: "shipped", id: "order-18" });
    expect(statusOf(next, "order-18"), "shipping a packed order should make it shipped").toBe("shipped");
  });

  it("won't ship an order that isn't packed", () => {
    const next = ordersReducer(board(), { type: "shipped", id: "order-17" });
    expect(statusOf(next, "order-17"), "a new order must be packed before it ships, so it should stay new").toBe("new");
  });

  it("cancels new and packed orders", () => {
    let orders = ordersReducer(board(), { type: "cancelled", id: "order-17" });
    orders = ordersReducer(orders, { type: "cancelled", id: "order-18" });
    expect(statusOf(orders, "order-17"), "a new order can be cancelled").toBe("cancelled");
    expect(statusOf(orders, "order-18"), "a packed order can be cancelled").toBe("cancelled");
  });

  it("won't cancel a shipped order, or ship a cancelled one", () => {
    let orders = ordersReducer(board(), { type: "cancelled", id: "order-19" });
    expect(statusOf(orders, "order-19"), "order-19 has shipped, so cancelling it should change nothing").toBe("shipped");
    orders = ordersReducer(orders, { type: "cancelled", id: "order-18" });
    orders = ordersReducer(orders, { type: "shipped", id: "order-18" });
    expect(statusOf(orders, "order-18"), "a cancelled order must never ship").toBe("cancelled");
  });

  it("never changes the old orders", () => {
    const orders = board();
    const before = JSON.stringify(orders);
    const next = ordersReducer(orders, { type: "packed", id: "order-17" });
    expect(JSON.stringify(orders), "the old array and its orders should be untouched (return new ones)").toBe(before);
    expect(next === orders, "a change should return a new array").toBe(false);
  });

  it("throws on an action it doesn't know", () => {
    expect(() => ordersReducer(board(), { type: "lost", id: "order-17" }), "an unknown action should throw Unknown action: lost (use assertNever)").toThrow(
      "Unknown action: lost",
    );
  });
});
