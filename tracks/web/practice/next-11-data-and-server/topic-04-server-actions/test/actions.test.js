import { describe, it, expect, beforeEach, vi } from "vitest";

vi.mock("next/cache", () => ({ revalidatePath: vi.fn(), revalidateTag: vi.fn(), updateTag: vi.fn() }));

import { revalidatePath } from "next/cache";
import { markPacked, cancelOrder } from "../src/app/orders/actions.js";
import { resetOrders, getOrder } from "../src/lib/orders.js";

beforeEach(() => {
  resetOrders();
  vi.clearAllMocks();
});

/** The FormData the Mark packed form sends. */
function packForm(orderId) {
  const form = new FormData();
  form.append("orderId", orderId);
  return form;
}

describe("markPacked", () => {
  it("packs a new order", async () => {
    const result = await markPacked(packForm("order-17"));
    expect(result, "packing order-17 should return { ok: true }").toEqual({ ok: true });
    expect(getOrder("order-17").status, "order-17 should now be PACKED (use setStatus)").toBe("PACKED");
  });

  it("refreshes the orders page after the change", async () => {
    await markPacked(packForm("order-19"));
    expect(revalidatePath.mock.calls, 'after packing, call revalidatePath("/orders")').toContainEqual(["/orders"]);
  });

  it("won't pack an order twice", async () => {
    const result = await markPacked(packForm("order-18"));
    expect(result, "order-18 is already PACKED, so return that as an error").toEqual({ error: "order-18 is already PACKED." });
    expect(revalidatePath.mock.calls.length, "a refused request changes nothing, so there's nothing to refresh").toBe(0);
  });

  it("won't send a cancelled order back to packed", async () => {
    await cancelOrder("order-17", new FormData());
    const result = await markPacked(packForm("order-17"));
    expect(result, "a CANCELLED order can't be packed").toEqual({ error: "order-17 is already CANCELLED." });
    expect(getOrder("order-17").status, "order-17 should stay CANCELLED").toBe("CANCELLED");
  });

  it("answers clearly for an order that doesn't exist", async () => {
    const result = await markPacked(packForm("order-99"));
    expect(result, "an unknown id should return { error: \"No order order-99.\" }, not crash").toEqual({ error: "No order order-99." });
  });
});

describe("cancelOrder", () => {
  it("cancels a new order, with the id as its first argument", async () => {
    const cancel19 = cancelOrder.bind(null, "order-19");
    const result = await cancel19(new FormData());
    expect(result, "cancelling order-19 should return { ok: true }").toEqual({ ok: true });
    expect(getOrder("order-19").status, "order-19 should now be CANCELLED").toBe("CANCELLED");
    expect(revalidatePath.mock.calls, 'after cancelling, call revalidatePath("/orders")').toContainEqual(["/orders"]);
  });

  it("won't cancel an order that is already packed", async () => {
    const result = await cancelOrder("order-18", new FormData());
    expect(result, "a PACKED order can't be cancelled").toEqual({ error: "order-18 can't be cancelled once it's PACKED." });
    expect(getOrder("order-18").status, "order-18 should stay PACKED").toBe("PACKED");
  });

  it("answers clearly for an order that doesn't exist", async () => {
    const result = await cancelOrder("order-99", new FormData());
    expect(result, "an unknown id should return { error: \"No order order-99.\" }").toEqual({ error: "No order order-99." });
  });
});
