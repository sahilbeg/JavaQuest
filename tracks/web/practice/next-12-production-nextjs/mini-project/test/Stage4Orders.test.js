import { describe, it, expect, beforeEach, vi } from "vitest";
import { placeOrder } from "../src/app/actions.js";
import { resetStore, createOrder, getItem, listOrders } from "../src/lib/store.js";

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
}));

const TOTALS = { subtotal: 90000, offer: 0, delivery: 0, total: 90000 };

function checkout({ lines = [], name = "Asha Patil", phone = "98765 43210" } = {}) {
  const data = new FormData();
  for (const line of lines) data.append("line", line);
  data.set("name", name);
  data.set("phone", phone);
  return data;
}

async function stockOf(id) {
  return (await getItem(id)).stock;
}

beforeEach(resetStore);

describe("Stage 4: createOrder", () => {
  it("saves order-1 and takes the items out of stock", async () => {
    const order = await createOrder({ name: "Asha Patil", phone: "9876543210", lines: [{ id: "DC-1042", qty: 2, paise: 45000 }], totals: TOTALS });
    expect(order?.id, "the first order should be order-1 (TODO 6)").toBe("order-1");
    expect(await stockOf("DC-1042"), "2 of Golu's 7 should be taken, leaving 5 (TODO 6)").toBe(5);
    expect((await listOrders()).length, "the order should be saved (TODO 6)").toBe(1);
  });

  it("numbers orders one after another", async () => {
    await createOrder({ name: "Asha Patil", phone: "9876543210", lines: [{ id: "DC-1101", qty: 1, paise: 15000 }], totals: TOTALS });
    const second = await createOrder({ name: "Imran Shaikh", phone: "9876543210", lines: [{ id: "DC-1101", qty: 1, paise: 15000 }], totals: TOTALS });
    expect(second?.id, "the second order should be order-2 (TODO 6)").toBe("order-2");
  });

  it("refuses the whole order when anything is short, and changes nothing", async () => {
    expect(await createOrder({ name: "A", phone: "9876543210", lines: [{ id: "DC-1042", qty: 1, paise: 45000 }, { id: "DC-1205", qty: 1, paise: 12000 }], totals: TOTALS }), "the keychain has 0 in stock, so the order should be refused with null (TODO 6)").toBeNull();
    expect(await stockOf("DC-1042"), "Golu's stock must not change when the order is refused (TODO 6)").toBe(7);
    expect(await createOrder({ name: "A", phone: "9876543210", lines: [{ id: "DC-1301", qty: 1, paise: 89900 }, { id: "DC-1301", qty: 2, paise: 89900 }], totals: TOTALS }), "1 + 2 tote bags is 3, but only 2 exist: null (TODO 6)").toBeNull();
    expect((await listOrders()).length, "refused orders must not be saved (TODO 6)").toBe(0);
  });
});

describe("Stage 4: the placeOrder action", () => {
  it("checks the cart, the name and the phone, in that order", async () => {
    expect(await placeOrder({ error: "" }, checkout({ lines: [] })), "no lines (TODO 7)").toEqual({ error: "Your cart is empty" });
    expect(await placeOrder({ error: "" }, checkout({ lines: ["DC-1042:1"], name: "   " })), "a blank name (TODO 7)").toEqual({ error: "Please enter your name" });
    expect(await placeOrder({ error: "" }, checkout({ lines: ["DC-1042:1"], phone: "98765 4321" })), "only 9 digits (TODO 7)").toEqual({
      error: "Please enter a 10-digit phone number",
    });
  });

  it("refuses lines it can't sell", async () => {
    for (const line of ["DC-9999:1", "DC-1042:0", "DC-1042:6", "DC-1042:abc", "DC-1042"]) {
      expect(await placeOrder({ error: "" }, checkout({ lines: [line] })), `the line "${line}" should be refused (TODO 7)`).toEqual({
        error: "Something in your cart can't be sold. Please check it.",
      });
    }
  });

  it("says so when an item has just sold out", async () => {
    expect(await placeOrder({ error: "" }, checkout({ lines: ["DC-1301:3"] })), "3 tote bags when only 2 exist (TODO 7)").toEqual({
      error: "Some items just sold out. Please check your cart.",
    });
  });

  it("saves the order with the store's prices and the offer rules, then goes to the thank-you page", async () => {
    await expect(
      placeOrder({ error: "" }, checkout({ lines: ["DC-1042:2", "DC-1301:1"], name: "  Asha Patil " })),
      "a good order should redirect to /orders/order-1 (TODO 7)"
    ).rejects.toThrow("NEXT_REDIRECT /orders/order-1");
    expect(await listOrders(), "the saved order should hold the trimmed name, the phone's 10 digits, server prices and the totals (TODO 7)").toEqual([
      {
        id: "order-1",
        name: "Asha Patil",
        phone: "9876543210",
        lines: [
          { id: "DC-1042", qty: 2, paise: 45000 },
          { id: "DC-1301", qty: 1, paise: 89900 },
        ],
        totals: { subtotal: 179900, offer: 17990, delivery: 0, total: 161910 },
      },
    ]);
    expect(await stockOf("DC-1301"), "one of the two tote bags should be taken (TODO 7)").toBe(1);
  });
});
