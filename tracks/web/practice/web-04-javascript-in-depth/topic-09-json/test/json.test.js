import { describe, it, expect } from "vitest";
import { parseCatalogue, catalogueToJson, stockSnapshot } from "../src/json.js";
import { CATALOGUE_JSON } from "../src/catalogueData.js";

/** Runs fn and gives back the message of what it threw, or null when it didn't throw. */
function thrownMessage(fn) {
  try {
    fn();
  } catch (error) {
    return error instanceof Error ? error.message : String(error);
  }
  return null;
}

describe("parseCatalogue", () => {
  it("turns the catalogue text into a list of six items", () => {
    const items = parseCatalogue(CATALOGUE_JSON);
    expect(Array.isArray(items) && items.length, "catalogue.json holds six items").toBe(6);
  });

  it("gives real objects with real numbers", () => {
    const items = parseCatalogue(CATALOGUE_JSON);
    const bag = items[3] || {};
    expect(bag.name, "the fourth item is the Indigo tote bag").toBe("Indigo tote bag");
    expect(bag.paise, "paise should be the number 89900, not text").toBe(89900);
  });

  it("explains when the text is not valid JSON", () => {
    const brokenComma = '[{ "id": "DC-1042", "stock": 3 },]';
    expect(thrownMessage(() => parseCatalogue(brokenComma)), "a trailing comma should give the friendly error").toBe(
      "catalogue.json is not valid JSON",
    );
    const singleQuotes = "[{ 'id': 'DC-1042' }]";
    expect(thrownMessage(() => parseCatalogue(singleQuotes)), "single quotes should give the friendly error").toBe(
      "catalogue.json is not valid JSON",
    );
  });

  it("refuses JSON that is not a list", () => {
    expect(thrownMessage(() => parseCatalogue('{ "id": "DC-1042" }')), "one object is not a list of items").toBe(
      "catalogue.json should hold a list of items",
    );
  });

  it("accepts an empty list", () => {
    const items = parseCatalogue("[]");
    expect(Array.isArray(items) && items.length === 0, "[] is a valid, empty catalogue").toBe(true);
  });
});

describe("catalogueToJson", () => {
  it("writes the items as JSON indented by 2 spaces", () => {
    const items = [{ id: "DC-1043", code: "CST", paise: 15000 }];
    const expected = '[\n  {\n    "id": "DC-1043",\n    "code": "CST",\n    "paise": 15000\n  }\n]';
    expect(catalogueToJson(items), "the JSON should be indented by 2 spaces, one property per line").toBe(expected);
  });

  it("can be parsed back into the same items", () => {
    const items = [
      { id: "DC-1042", name: "Golu the elephant", stock: 3 },
      { id: "DC-1044", name: "Mint owl keychain", stock: 0 },
    ];
    const text = catalogueToJson(items);
    expect(text.length > 0 && JSON.parse(text), "parsing the JSON should give the same items back").toEqual(items);
  });
});

describe("stockSnapshot", () => {
  it("keeps only the id and stock of each item, as compact JSON", () => {
    const items = [
      { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 3 },
      { id: "DC-1043", name: "Marigold coaster set", paise: 15000, stock: 12 },
    ];
    expect(stockSnapshot(items), "the snapshot should hold only ids and stock, with no spaces").toBe(
      '[{"id":"DC-1042","stock":3},{"id":"DC-1043","stock":12}]',
    );
  });

  it("works on the whole catalogue", () => {
    const text = stockSnapshot(JSON.parse(CATALOGUE_JSON));
    expect(text, "six items should give six id and stock pairs").toBe(
      '[{"id":"DC-1042","stock":3},{"id":"DC-1043","stock":12},{"id":"DC-1044","stock":0},' +
        '{"id":"DC-1045","stock":5},{"id":"DC-1046","stock":4},{"id":"DC-1047","stock":0}]',
    );
  });
});
