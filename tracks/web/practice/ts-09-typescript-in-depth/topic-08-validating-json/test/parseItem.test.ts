import { describe, it, expect } from "vitest";
import { isRecord, parseItem, parseItems } from "../src/parseItem.ts";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 3 };

describe("isRecord", () => {
  it("says true for a plain object", () => {
    expect(isRecord({ id: "DC-1042" }), "a plain object is a record").toBe(true);
  });

  it("says false for null, arrays, text and numbers", () => {
    expect(isRecord(null), "null is not a record, even though typeof null is \"object\"").toBe(false);
    expect(isRecord([golu]), "an array is not a record").toBe(false);
    expect(isRecord("DC-1042"), "text is not a record").toBe(false);
    expect(isRecord(45000), "a number is not a record").toBe(false);
  });
});

describe("parseItem", () => {
  it("accepts a good item", () => {
    expect(parseItem({ ...golu }), "Golu is a good item, so parseItem should give ok and the item").toEqual({ ok: true, value: golu });
  });

  it("accepts a price and stock of 0", () => {
    const free = { id: "DC-1050", name: "Sample coaster", paise: 0, stock: 0 };
    expect(parseItem(free), "0 is a real whole number, so it should be accepted").toEqual({ ok: true, value: free });
  });

  it("leaves out fields the store doesn't know", () => {
    const result = parseItem({ ...golu, note: "from WhatsApp" });
    expect(result, "the extra note field should not be copied into the item").toEqual({ ok: true, value: golu });
  });

  it("refuses something that isn't an object", () => {
    expect(parseItem(null), "null should give \"not an object\"").toEqual({ ok: false, error: "not an object" });
    expect(parseItem([golu]), "an array should give \"not an object\"").toEqual({ ok: false, error: "not an object" });
  });

  it("refuses an id that doesn't look like DC-1042", () => {
    expect(parseItem({ ...golu, id: "1042" }), "an id without DC- should be refused").toEqual({ ok: false, error: "id must be text like DC-1042" });
    expect(parseItem({ ...golu, id: 1042 }), "a number id should be refused").toEqual({ ok: false, error: "id must be text like DC-1042" });
  });

  it("refuses a missing or empty name", () => {
    expect(parseItem({ id: "DC-1042", paise: 45000, stock: 3 }), "an item with no name should be refused").toEqual({ ok: false, error: "name must be text" });
    expect(parseItem({ ...golu, name: "   " }), "a name of only spaces should be refused").toEqual({ ok: false, error: "name must be text" });
  });

  it("refuses a price written as text", () => {
    expect(parseItem({ ...golu, paise: "45000" }), "\"45000\" in quotes is text, not a number").toEqual({ ok: false, error: "paise must be a whole number of 0 or more" });
  });

  it("refuses a price that isn't a whole number of 0 or more", () => {
    expect(parseItem({ ...golu, paise: 450.5 }), "paise must be whole").toEqual({ ok: false, error: "paise must be a whole number of 0 or more" });
    expect(parseItem({ ...golu, paise: -100 }), "a price can't be negative").toEqual({ ok: false, error: "paise must be a whole number of 0 or more" });
  });

  it("refuses a bad stock count", () => {
    expect(parseItem({ ...golu, stock: -1 }), "stock can't be negative").toEqual({ ok: false, error: "stock must be a whole number of 0 or more" });
    expect(parseItem({ ...golu, stock: null }), "stock can't be null").toEqual({ ok: false, error: "stock must be a whole number of 0 or more" });
  });
});

describe("parseItems", () => {
  it("accepts a good import file", () => {
    const keychain = { id: "DC-1044", name: "Mint keychain", paise: 12000, stock: 8 };
    const result = parseItems(JSON.stringify([golu, keychain]));
    expect(result, "both items are good, so the list should come back in order").toEqual({ ok: true, value: [golu, keychain] });
  });

  it("accepts an empty list", () => {
    expect(parseItems("[]"), "an empty list is a good (if boring) file").toEqual({ ok: true, value: [] });
  });

  it("says when the file isn't JSON", () => {
    expect(parseItems("Golu, 450, 3"), "plain text is not JSON").toEqual({ ok: false, error: "not valid JSON" });
  });

  it("says when the JSON isn't a list", () => {
    expect(parseItems(JSON.stringify(golu)), "one object on its own is not a list of items").toEqual({ ok: false, error: "expected a list of items" });
  });

  it("names the first bad item, counting from 1", () => {
    const bad = { id: "DC-1043", name: "Marigold coaster set", paise: "15000", stock: 2 };
    expect(parseItems(JSON.stringify([golu, bad])), "the second item has its price as text").toEqual({ ok: false, error: "item 2: paise must be a whole number of 0 or more" });
  });
});
