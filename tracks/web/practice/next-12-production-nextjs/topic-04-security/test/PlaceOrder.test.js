import { describe, it, expect, beforeEach } from "vitest";
import { placeOrder } from "../src/app/actions.js";
import { resetOrders, listOrders } from "../src/lib/orders.js";

function form(fields) {
  const data = new FormData();
  for (const [name, value] of Object.entries(fields)) data.set(name, value);
  return data;
}

beforeEach(resetOrders);

describe("placeOrder", () => {
  it("charges the server's price, even when the form sends a fake one", async () => {
    const result = await placeOrder(form({ id: "DC-1042", qty: "2", paise: "1" }));
    expect(result, "two Golus cost 2 × 45000 paise, whatever the hidden paise field says (TODO 1)").toEqual({ ok: true, orderId: "order-18", totalPaise: 90000 });
  });

  it("still works when the form sends no price at all", async () => {
    const result = await placeOrder(form({ id: "DC-1101", qty: "1" }));
    expect(result, "one coaster set costs 15000 paise from the catalogue (TODO 1)").toEqual({ ok: true, orderId: "order-18", totalPaise: 15000 });
  });

  it("saves the order with the real item id", async () => {
    await placeOrder(form({ id: "DC-1042", qty: "2", paise: "1" }));
    const saved = listOrders().find((order) => order.id === "order-18");
    expect(saved?.itemId, "order-18 should be saved for DC-1042").toBe("DC-1042");
    expect(saved?.totalPaise, "order-18 should be saved with the server's total, 90000 paise (TODO 1)").toBe(90000);
  });

  it("refuses an item that doesn't exist and saves nothing", async () => {
    const result = await placeOrder(form({ id: "DC-9999", qty: "1", paise: "100" }));
    expect(result, "an unknown id should give { ok: false, error: \"Unknown item\" } (TODO 2)").toEqual({ ok: false, error: "Unknown item" });
    expect(listOrders().length, "a refused order must not be saved: only order-17 should exist (TODO 2)").toBe(1);
  });

  it("refuses quantities that aren't a whole number from 1 to 5", async () => {
    for (const qty of ["0", "6", "-1", "2.5", "two", ""]) {
      expect(await placeOrder(form({ id: "DC-1042", qty })), `qty "${qty}" should be refused (TODO 2)`).toEqual({ ok: false, error: "Pick a quantity from 1 to 5" });
    }
    expect(listOrders().length, "refused orders must not be saved: only order-17 should exist (TODO 2)").toBe(1);
  });
});
