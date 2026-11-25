import { describe, it, expect, beforeEach } from "vitest";
import { createOrder, resetOrders, getOrder, listOrders } from "../src/lib/orders.js";

beforeEach(() => {
  resetOrders();
});

describe("createOrder", () => {
  it("saves a NEW order with the first number, order-20", () => {
    const order = createOrder({ name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-1042", qty: 1 }] });
    expect(order?.id, "the first order should be order-20").toBe("order-20");
    expect(order?.status, "a new order is NEW").toBe("NEW");
    expect(getOrder("order-20")?.name, "the order should be saved").toBe("Asha Rao");
  });

  it("prices from the catalogue and adds ₹60 delivery to small orders", () => {
    const order = createOrder({ name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-1042", qty: 1, paise: 1 }] });
    expect(order?.totalPaise, "Golu at the catalogue price, 45000, plus 6000 delivery (ignore the paise sent with the line)").toBe(51000);
    expect(getOrder("order-20")?.lines, "each saved line keeps only itemId and qty").toEqual([{ itemId: "DC-1042", qty: 1 }]);
  });

  it("delivers free when the items come to more than ₹999", () => {
    const order = createOrder({ name: "Ravi Joshi", phone: "9876543211", lines: [{ itemId: "DC-3007", qty: 2 }] });
    expect(order?.totalPaise, "two tote bags are 179800 paise, above 99900, so delivery is free").toBe(179800);
  });

  it("refuses an unknown item and saves nothing", () => {
    expect(
      () => createOrder({ name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-9999", qty: 1 }] }),
      "an unknown item should throw Unknown item DC-9999.",
    ).toThrow("Unknown item DC-9999.");
    expect(listOrders().length, "nothing should be saved").toBe(0);
  });

  it("gives each new order the next number", () => {
    createOrder({ name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-1042", qty: 1 }] });
    const second = createOrder({ name: "Ravi Joshi", phone: "9876543211", lines: [{ itemId: "DC-2001", qty: 1 }] });
    expect(second?.id, "the second order should be order-21").toBe("order-21");
    expect(listOrders().map((o) => o.id), "listOrders shows the newest first").toEqual(["order-21", "order-20"]);
  });
});
