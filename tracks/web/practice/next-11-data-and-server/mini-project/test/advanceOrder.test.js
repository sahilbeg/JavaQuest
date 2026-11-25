import { describe, it, expect, beforeEach, vi } from "vitest";

vi.mock("next/cache", () => ({ revalidatePath: vi.fn(), revalidateTag: vi.fn(), updateTag: vi.fn() }));

import { revalidatePath } from "next/cache";
import { advanceOrder } from "../src/app/orders/actions.js";
import { resetOrders, getOrder, setStatus, createOrder } from "../src/lib/orders.js";

function form(orderId) {
  const data = new FormData();
  data.append("orderId", orderId);
  return data;
}

beforeEach(() => {
  resetOrders();
  vi.clearAllMocks();
});

/** order-20, made with createOrder if it works, so these checks can run on their own. */
function anOrder() {
  const order = createOrder({ name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-1042", qty: 1 }] });
  return order?.id ?? "order-20";
}

describe("advanceOrder", () => {
  it("moves a NEW order to PACKED and refreshes the page", async () => {
    const id = anOrder();
    const result = await advanceOrder(form(id));
    expect(result, "NEW moves to PACKED, so return { ok: true, status: \"PACKED\" }").toEqual({ ok: true, status: "PACKED" });
    expect(getOrder(id)?.status, "the order should now be PACKED").toBe("PACKED");
    expect(revalidatePath.mock.calls, 'after the change, call revalidatePath("/orders")').toContainEqual(["/orders"]);
  });

  it("moves one step at a time, all the way to DELIVERED", async () => {
    const id = anOrder();
    const steps = [];
    for (let i = 0; i < 3; i++) steps.push((await advanceOrder(form(id)))?.status);
    expect(steps, "three presses go PACKED, SHIPPED, DELIVERED").toEqual(["PACKED", "SHIPPED", "DELIVERED"]);
  });

  it("refuses to move a delivered order", async () => {
    const id = anOrder();
    if (getOrder(id)) setStatus(id, "DELIVERED");
    const result = await advanceOrder(form(id));
    expect(result, "a DELIVERED order is finished").toEqual({ error: `${id} is already DELIVERED.` });
    expect(revalidatePath.mock.calls.length, "nothing changed, so nothing to refresh").toBe(0);
  });

  it("answers clearly for an order that doesn't exist", async () => {
    expect(await advanceOrder(form("order-99")), "an unknown id should return { error: \"No order order-99.\" }").toEqual({
      error: "No order order-99.",
    });
  });
});
