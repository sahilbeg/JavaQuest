import { describe, it, expect } from "vitest";
import { orderRequest, parseItem, parseItems } from "../src/api.ts";

const GOLU_JSON = { id: "DC-1042", name: "Golu the elephant", price_paise: 45000, stock: 7 };
const TOTE_JSON = { id: "DC-1301", name: "Rani pink tote bag", price_paise: 89900, stock: 0 };
const GOLU = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 7 };
const TOTE = { id: "DC-1301", name: "Rani pink tote bag", paise: 89900, stock: 0 };

describe("parseItem", () => {
  it("turns the server's item into the app's Item", () => {
    expect(parseItem(GOLU_JSON), "price_paise from the server should become paise in the app (TODO 1)").toEqual(GOLU);
  });

  it("keeps only the fields the app uses", () => {
    const item = parseItem({ ...GOLU_JSON, warehouse_note: "shelf 4" });
    expect(item, "extra fields from the server should not leak into the Item: build a new object (TODO 1)").toEqual(GOLU);
  });

  it("accepts an item with no stock left", () => {
    expect(parseItem(TOTE_JSON), "stock 0 is a real answer: the tote bag is sold out (TODO 1)").toEqual(TOTE);
  });

  it("refuses anything that isn't an item object", () => {
    for (const data of [null, undefined, "DC-1042", 45000, [GOLU_JSON]]) {
      expect(parseItem(data), `${JSON.stringify(data)} is not an item, so parseItem should give null`).toBe(null);
    }
  });

  it("refuses an item with a field of the wrong type or a missing field", () => {
    expect(parseItem({ ...GOLU_JSON, price_paise: "45000" }), "a price sent as text must be refused, or totals become \"45000\" + ... (TODO 1)").toBe(null);
    expect(parseItem({ ...GOLU_JSON, id: "" }), "an empty id must be refused").toBe(null);
    expect(parseItem({ id: "DC-1042", name: "Golu the elephant", stock: 7 }), "an item without price_paise must be refused").toBe(null);
    expect(parseItem({ ...GOLU_JSON, name: 42 }), "a name that isn't text must be refused").toBe(null);
  });

  it("refuses prices and stock that aren't whole numbers of 0 or more", () => {
    expect(parseItem({ ...GOLU_JSON, price_paise: 450.5 }), "paise are always whole numbers").toBe(null);
    expect(parseItem({ ...GOLU_JSON, stock: -1 }), "stock can't be below 0").toBe(null);
    expect(parseItem({ ...GOLU_JSON, stock: 2.5 }), "stock is a whole number of items").toBe(null);
  });
});

describe("parseItems", () => {
  it("parses a whole list", () => {
    expect(parseItems([GOLU_JSON, TOTE_JSON]), "two good items should give two Items, in order (TODO 2)").toEqual([GOLU, TOTE]);
    expect(parseItems([]), "an empty list is a good answer: no items (TODO 2)").toEqual([]);
  });

  it("refuses the list if any item is bad, or if it isn't a list", () => {
    expect(parseItems([GOLU_JSON, { ...TOTE_JSON, price_paise: null }]), "one bad item should make the whole list null").toBe(null);
    expect(parseItems({ items: [GOLU_JSON] }), "an object is not a list").toBe(null);
    expect(parseItems(null), "null is not a list").toBe(null);
  });
});

describe("orderRequest", () => {
  it("writes the order in the server's names", () => {
    const body = orderRequest([{ id: "DC-1042", qty: 2 }, { id: "DC-1301", qty: 1 }], "98765 43210");
    expect(body, "each line becomes { item_id, qty }, and the phone loses its spaces (TODO 4)").toEqual({
      items: [
        { item_id: "DC-1042", qty: 2 },
        { item_id: "DC-1301", qty: 1 },
      ],
      phone: "9876543210",
    });
  });

  it("leaves out lines with nothing in them", () => {
    const body = orderRequest([{ id: "DC-1042", qty: 0 }, { id: "DC-1301", qty: 1 }], "9876543210");
    expect(body.items, "a line with qty 0 should not be sent (TODO 4)").toEqual([{ item_id: "DC-1301", qty: 1 }]);
  });
});
