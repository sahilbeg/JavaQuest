import { describe, it, expect, beforeEach } from "vitest";
import { toOrderView, getOrders, getOrderView, createOrder } from "../src/lib/orders.js";
import { resetDb, dbAll, dbFind } from "../src/lib/db.js";

beforeEach(() => {
  resetDb();
});

const ROW = { id: "order-17", name: "Asha Rao", phone: "9876543210", lines: [], totalPaise: 41000, status: "NEW", upiRef: "UPI-55120" };

describe("toOrderView", () => {
  it("gives a page what it needs to show", () => {
    expect(toOrderView(ROW), "the view of order-17 should have its id, customer, phone ending, total in rupees and status").toEqual({
      id: "order-17",
      customer: "Asha Rao",
      phoneEnding: "3210",
      total: "₹410.00",
      status: "NEW",
    });
  });

  it("never hands out the full phone, the UPI reference or any other field", () => {
    const view = toOrderView({ ...ROW, notes: "asked for gift wrap" });
    expect(Object.keys(view ?? {}).sort(), "a view should have exactly these five fields (list them by name, don't spread the row)").toEqual([
      "customer",
      "id",
      "phoneEnding",
      "status",
      "total",
    ]);
  });
});

describe("getOrders", () => {
  it("lists every order, newest first", () => {
    expect(getOrders().map((order) => order?.id), "the three orders should come newest first").toEqual(["order-19", "order-18", "order-17"]);
  });

  it("lists only orders with the status asked for", () => {
    expect(getOrders({ status: "NEW" }).map((order) => order?.id), "only order-19 and order-17 are NEW").toEqual(["order-19", "order-17"]);
  });

  it("gives views, not rows", () => {
    const first = getOrders()[0];
    expect(first?.customer, "each order should be a view, with customer").toBe("Neha Desai");
    expect(first && "phone" in first, "a view must not carry the full phone").toBe(false);
  });
});

describe("getOrderView", () => {
  it("gives the view of one order", () => {
    expect(getOrderView("order-18"), "order-18 should come back as a view").toEqual({
      id: "order-18",
      customer: "Ravi Joshi",
      phoneEnding: "3211",
      total: "₹510.00",
      status: "PACKED",
    });
  });

  it("gives null for an order that doesn't exist", () => {
    expect(getOrderView("order-99"), "an unknown id should give null").toBeNull();
  });
});

describe("createOrder", () => {
  it("prices the order from the catalogue and ignores prices sent with it", () => {
    const view = createOrder({
      name: "Farah Khan",
      phone: "9876543213",
      lines: [
        { itemId: "DC-4410", qty: 2 },
        { itemId: "DC-1042", qty: 1, paise: 1 },
      ],
    });
    expect(view?.total, "2 booties and Golu at catalogue prices make ₹1,150.00, with free delivery").toBe("₹1,150.00");
    expect(dbFind("orders", "order-20")?.totalPaise, "the saved row should hold 115000 paise").toBe(115000);
  });

  it("saves a NEW order with the next id and only the line fields it needs", () => {
    const view = createOrder({ name: "Farah Khan", phone: "9876543213", lines: [{ itemId: "DC-1042", qty: 1, paise: 1 }] });
    expect(view?.id, "the next id is order-20").toBe("order-20");
    expect(view?.status, "a new order is NEW").toBe("NEW");
    const row = dbFind("orders", "order-20");
    expect(row?.lines, "each saved line should hold only itemId and qty").toEqual([{ itemId: "DC-1042", qty: 1 }]);
    expect(row?.upiRef, "a new order has not been paid yet, so upiRef is null").toBeNull();
  });

  it("charges ₹60 delivery for small orders", () => {
    const view = createOrder({ name: "Farah Khan", phone: "9876543213", lines: [{ itemId: "DC-5120", qty: 1 }] });
    expect(view?.total, "a ₹120 keychain plus ₹60 delivery makes ₹180.00").toBe("₹180.00");
  });

  it("refuses an unknown item and saves nothing", () => {
    expect(
      () => createOrder({ name: "Farah Khan", phone: "9876543213", lines: [{ itemId: "DC-1042", qty: 1 }, { itemId: "DC-9999", qty: 1 }] }),
      "an unknown item should throw Unknown item DC-9999.",
    ).toThrow("Unknown item DC-9999.");
    expect(dbAll("orders").length, "nothing should be saved when an item is unknown").toBe(3);
  });

  it("puts the new order first in the list", () => {
    createOrder({ name: "Farah Khan", phone: "9876543213", lines: [{ itemId: "DC-2001", qty: 1 }] });
    expect(getOrders()[0]?.id, "order-20 is the newest").toBe("order-20");
  });
});
