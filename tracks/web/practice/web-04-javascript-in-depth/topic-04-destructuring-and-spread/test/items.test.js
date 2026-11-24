import { describe, it, expect } from "vitest";
import { cardLabel, withStock, mergeCatalogues, splitFeatured, totalPaise } from "../src/items.js";

const golu = { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 };
const bag = { id: "DC-1045", code: "BAG", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 };
const plainKey = { id: "DC-1048", code: "KEY", name: "Plain owl keychain", paise: 12000, stock: 7 };

describe("cardLabel", () => {
  it("shows the name, the colour and the price in rupees", () => {
    expect(cardLabel(golu), "Golu's card should read: Golu the elephant (Indigo): ₹450.00").toBe(
      "Golu the elephant (Indigo): ₹450.00",
    );
    expect(cardLabel(bag), "the bag's card should read: Indigo tote bag (Indigo): ₹899.00").toBe(
      "Indigo tote bag (Indigo): ₹899.00",
    );
  });

  it("says Off-white when an item has no colour", () => {
    expect(cardLabel(plainKey), "an item with no colour should show Off-white, not undefined").toBe(
      "Plain owl keychain (Off-white): ₹120.00",
    );
  });
});

describe("withStock", () => {
  it("returns a copy of the item with the new stock", () => {
    expect(withStock(golu, 2), "Golu with stock 2 should keep every other field").toEqual({
      id: "DC-1042",
      code: "AMI",
      name: "Golu the elephant",
      colour: "Indigo",
      paise: 45000,
      stock: 2,
    });
  });

  it("does not change the item it was given", () => {
    const item = { ...bag };
    const result = withStock(item, 0);
    expect(result === item, "withStock should return a new object, not the same one").toBe(false);
    expect(item.stock, "the original bag must still have 5 in stock").toBe(5);
  });
});

describe("mergeCatalogues", () => {
  it("puts the stall's items first, then the shop's", () => {
    const merged = mergeCatalogues([golu], [bag, plainKey]);
    expect(merged.map((item) => item.id).join(","), "one stall item, then two shop items").toBe("DC-1042,DC-1045,DC-1048");
  });

  it("returns a new array and leaves both lists alone", () => {
    const stall = [golu];
    const shop = [bag];
    const merged = mergeCatalogues(stall, shop);
    expect(merged === stall || merged === shop, "the merged list should be a new array").toBe(false);
    expect(stall.length + shop.length, "the stall and shop lists must keep one item each").toBe(2);
  });
});

describe("splitFeatured", () => {
  it("takes the first item as featured and keeps the rest in order", () => {
    const { featured, others } = splitFeatured([golu, bag, plainKey]);
    expect(featured, "the first item should be featured").toBe(golu);
    expect(others.map((item) => item.id).join(","), "the other two items should follow, in order").toBe("DC-1045,DC-1048");
  });

  it("handles a list with one item", () => {
    const { featured, others } = splitFeatured([bag]);
    expect(featured, "the only item should be featured").toBe(bag);
    expect(others.length, "nothing is left over").toBe(0);
  });

  it("handles an empty list", () => {
    const { featured, others } = splitFeatured([]);
    expect(featured, "an empty list has no featured item").toBeUndefined();
    expect(Array.isArray(others) && others.length === 0, "others should be an empty array").toBe(true);
  });
});

describe("totalPaise", () => {
  it("adds up any number of prices", () => {
    expect(totalPaise(15000, 12000), "a coaster set and a keychain cost 27000 paise").toBe(27000);
    expect(totalPaise(45000, 3000, 5000), "Golu with gift wrap and a stitched name costs 53000 paise").toBe(53000);
  });

  it("returns 0 when given no prices", () => {
    expect(totalPaise(), "no prices add up to 0").toBe(0);
  });
});
