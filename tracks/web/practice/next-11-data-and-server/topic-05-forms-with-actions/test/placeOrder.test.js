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

import { placeOrder } from "../src/app/order/actions.js";
import { resetOrders, getOrder, countOrders } from "../src/lib/orders.js";

const START = { errors: {}, values: { name: "", phone: "", itemId: "DC-1042", qty: "1" } };
const GOOD = { name: "Asha Rao", phone: "98765 43210", itemId: "DC-4410", qty: "2" };

/** The FormData the order form sends with these fields. */
function form(fields) {
  const data = new FormData();
  for (const [name, value] of Object.entries(fields)) data.append(name, value);
  return data;
}

beforeEach(() => {
  resetOrders();
});

describe("placeOrder with good details", () => {
  it("saves the order and moves to its page", async () => {
    await expect(placeOrder(START, form(GOOD)), "a good order should end with redirect(\"/order/order-20\")").rejects.toThrow(
      "NEXT_REDIRECT /order/order-20",
    );
    expect(countOrders(), "exactly one order should be saved (use createOrder)").toBe(1);
  });

  it("saves cleaned values: name trimmed, phone without spaces, quantity as a number", async () => {
    await placeOrder(START, form({ ...GOOD, name: "  Asha Rao  " })).catch(() => {});
    const order = getOrder("order-20");
    expect(order, "order-20 should have been saved").toBeDefined();
    expect(order?.name, "the name should be saved trimmed").toBe("Asha Rao");
    expect(order?.phone, "the phone should be saved without spaces").toBe("9876543210");
    expect(order?.qty, "the quantity should be saved as the number 2, not the text \"2\"").toBe(2);
    expect(order?.itemId, "the item id should be saved as chosen").toBe("DC-4410");
  });
});

describe("placeOrder with mistakes", () => {
  it("explains an empty name and saves nothing", async () => {
    const result = await placeOrder(START, form({ ...GOOD, name: "   " }));
    expect(result?.errors?.name, "a name of only spaces should give Please enter your name.").toBe("Please enter your name.");
    expect(countOrders(), "nothing should be saved when there is a mistake").toBe(0);
  });

  it("explains a phone number without 10 digits", async () => {
    const result = await placeOrder(START, form({ ...GOOD, phone: "98765 4321" }));
    expect(result?.errors?.phone, "9 digits should give the phone message").toBe("Phone needs 10 digits, like 98765 43210.");
  });

  it("explains an item that isn't in the catalogue", async () => {
    const result = await placeOrder(START, form({ ...GOOD, itemId: "DC-9999" }));
    expect(result?.errors?.itemId, "an unknown item should give Please choose an item.").toBe("Please choose an item.");
  });

  it("explains a quantity outside 1 to 5", async () => {
    for (const qty of ["0", "6", "2.5", ""]) {
      const result = await placeOrder(START, form({ ...GOOD, qty }));
      expect(result?.errors?.qty, `quantity "${qty}" should give Choose between 1 and 5.`).toBe("Choose between 1 and 5.");
    }
    expect(countOrders(), "no order should be saved for a bad quantity").toBe(0);
  });

  it("reports every mistake at once and sends back what was typed", async () => {
    const typed = { name: "", phone: "123", itemId: "DC-1042", qty: "9" };
    const result = await placeOrder(START, form(typed));
    expect(Object.keys(result?.errors ?? {}).sort(), "name, phone and qty are all wrong, so all three need a message").toEqual(["name", "phone", "qty"]);
    expect(result?.values, "values should hold exactly what was typed, so the form can refill it").toEqual(typed);
  });
});
