import { describe, it, expect } from "vitest";
import { matches, runQuery, countBy, toCard } from "../src/catalogue.ts";
import type { Filter } from "../src/catalogue.ts";
import { catalogue, ids } from "./items.ts";

const golu = catalogue()[0];

describe("matches", () => {
  it("checks the colour", () => {
    expect(matches(golu, { by: "colour", colour: "Indigo" }), "Golu is Indigo").toBe(true);
    expect(matches(golu, { by: "colour", colour: "Mint" }), "Golu is not Mint").toBe(false);
  });

  it("checks the price range, ends included", () => {
    expect(matches(golu, { by: "price", minPaise: 45000, maxPaise: 45000 }), "45000 is inside 45000 to 45000").toBe(true);
    expect(matches(golu, { by: "price", maxPaise: 40000 }), "Golu costs more than 40000").toBe(false);
    expect(matches(golu, { by: "price", minPaise: 50000 }), "Golu costs less than 50000").toBe(false);
    expect(matches(golu, { by: "price" }), "a price filter with no limits lets everything through").toBe(true);
  });

  it("checks the stock", () => {
    expect(matches(golu, { by: "inStock" }), "Golu has 3 in stock").toBe(true);
    expect(matches(catalogue()[1], { by: "inStock" }), "the Marigold coaster set has 0 in stock").toBe(false);
  });

  it("searches names, ignoring capitals and spaces around the text", () => {
    expect(matches(golu, { by: "search", text: "  GOLU " }), "\"  GOLU \" should find Golu the elephant").toBe(true);
    expect(matches(golu, { by: "search", text: "parrot" }), "Golu is not a parrot").toBe(false);
    expect(matches(golu, { by: "search", text: "" }), "an empty search matches everything").toBe(true);
  });

  it("throws for a filter the engine doesn't know", () => {
    const unknown = { by: "code", code: "AMI" } as unknown as Filter;
    expect(() => matches(golu, unknown), "an unknown filter should throw through assertNever").toThrow("Unknown filter");
  });
});

describe("runQuery", () => {
  it("answers: gifts under ₹500 that you have today, cheapest first", () => {
    const result = runQuery(catalogue(), {
      filters: [{ by: "price", maxPaise: 50000 }, { by: "inStock" }],
      sort: { key: "paise", direction: "asc" },
    });
    expect(ids(result), "the keychain, the Off-white coasters, then Golu and the parrot (same price, catalogue order)").toEqual(["DC-1044", "DC-1049", "DC-1042", "DC-1046"]);
  });

  it("keeps catalogue order when there is no sort", () => {
    const result = runQuery(catalogue(), { filters: [{ by: "colour", colour: "Mint" }] });
    expect(ids(result), "the two Mint items, in catalogue order").toEqual(["DC-1044", "DC-1047"]);
  });

  it("gives every item for a query with no filters", () => {
    expect(runQuery(catalogue(), { filters: [] }).length, "no filters means every item").toBe(8);
  });

  it("treats a price limit of 0 as a real limit", () => {
    expect(runQuery(catalogue(), { filters: [{ by: "price", maxPaise: 0 }] }), "nothing costs ₹0 or less").toEqual([]);
  });

  it("sorts by name, A to Z", () => {
    const result = runQuery(catalogue(), { filters: [{ by: "inStock" }], sort: { key: "name", direction: "asc" } });
    expect(ids(result), "Golu, Indigo tote bag, Mint keychain, Off-white coaster set, Rani pink parrot").toEqual(["DC-1042", "DC-1045", "DC-1044", "DC-1049", "DC-1046"]);
  });

  it("sorts by stock, most first, keeping ties in catalogue order", () => {
    const result = runQuery(catalogue(), { filters: [], sort: { key: "stock", direction: "desc" } });
    expect(ids(result), "8, 5, 3, 2, 1, then the three items with 0 in catalogue order").toEqual(["DC-1044", "DC-1049", "DC-1042", "DC-1045", "DC-1046", "DC-1043", "DC-1047", "DC-1048"]);
  });

  it("never changes the catalogue it was given", () => {
    const items = catalogue();
    runQuery(items, { filters: [{ by: "inStock" }], sort: { key: "paise", direction: "desc" } });
    expect(ids(items), "the catalogue must keep its order and all 8 items").toEqual(ids(catalogue()));
  });
});

describe("countBy", () => {
  it("counts the items per colour, in the order colours first appear", () => {
    const counts = countBy(catalogue(), "colour");
    expect([...counts.entries()], "Indigo 2, Marigold 2, Mint 2, Rani pink 1, Off-white 1").toEqual([
      ["Indigo", 2],
      ["Marigold", 2],
      ["Mint", 2],
      ["Rani pink", 1],
      ["Off-white", 1],
    ]);
  });

  it("counts by any key, like the item code", () => {
    const counts = countBy(catalogue(), "code");
    expect(counts.get("BAG"), "there are two tote bags").toBe(2);
    expect(counts.get("KEY"), "there is one keychain").toBe(1);
  });
});

describe("toCard", () => {
  it("shows the price in rupees", () => {
    expect(toCard(golu), "Golu's card shows ₹450.00 and is not sold out").toEqual({ id: "DC-1042", name: "Golu the elephant", price: "₹450.00", soldOut: false });
  });

  it("marks an item with no stock as sold out", () => {
    expect(toCard(catalogue()[6]), "the Marigold tote bag has 0 in stock").toEqual({ id: "DC-1048", name: "Marigold tote bag", price: "₹899.00", soldOut: true });
  });
});
