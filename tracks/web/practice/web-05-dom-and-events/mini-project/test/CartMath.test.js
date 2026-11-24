import { describe, it, expect } from "vitest";
import { countItems, cartTotal, changeQty } from "../src/drawer.js";

const CATALOGUE = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1001", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1013", name: "Rani pink keychain", paise: 12000 },
];

describe("countItems", () => {
  it("counts every piece, not every line", () => {
    expect(countItems([{ id: "DC-1042", qty: 2 }, { id: "DC-1001", qty: 1 }]), "2 Golus and 1 coaster set are 3 pieces").toBe(3);
  });

  it("counts zero for an empty cart", () => {
    expect(countItems([]), "an empty cart has 0 pieces").toBe(0);
  });
});

describe("cartTotal", () => {
  it("adds price times quantity for every line", () => {
    expect(cartTotal([{ id: "DC-1042", qty: 2 }, { id: "DC-1001", qty: 1 }], CATALOGUE), "2 × 45000 + 1 × 15000 is 105000 paise").toBe(105000);
  });

  it("is zero for an empty cart", () => {
    expect(cartTotal([], CATALOGUE), "an empty cart totals 0 paise").toBe(0);
  });
});

describe("changeQty", () => {
  const LINES = [{ id: "DC-1042", qty: 2 }, { id: "DC-1001", qty: 1 }];

  it("adds a new item at the end with a quantity of 1", () => {
    expect(changeQty(LINES, "DC-1013", 1), "a new keychain should be added after the other lines").toEqual([
      { id: "DC-1042", qty: 2 },
      { id: "DC-1001", qty: 1 },
      { id: "DC-1013", qty: 1 },
    ]);
  });

  it("adds one to an item already in the cart", () => {
    expect(changeQty(LINES, "DC-1042", 1), "one more Golu should make 3 Golus on the same line").toEqual([
      { id: "DC-1042", qty: 3 },
      { id: "DC-1001", qty: 1 },
    ]);
  });

  it("takes one away", () => {
    expect(changeQty(LINES, "DC-1042", -1), "one less Golu should leave 1").toEqual([
      { id: "DC-1042", qty: 1 },
      { id: "DC-1001", qty: 1 },
    ]);
  });

  it("removes a line that reaches zero", () => {
    expect(changeQty(LINES, "DC-1001", -1), "the last coaster set taken away removes its line").toEqual([{ id: "DC-1042", qty: 2 }]);
  });

  it("ignores taking away an item that isn't in the cart", () => {
    expect(changeQty(LINES, "DC-1013", -1), "there is no keychain to take away, so nothing changes").toEqual(LINES);
  });

  it("returns a new array and leaves the old one alone", () => {
    const before = [{ id: "DC-1042", qty: 2 }];
    const after = changeQty(before, "DC-1042", 1);
    expect(after !== before, "changeQty should return a new array, not the same one").toBe(true);
    expect(before, "the old lines must not change: build new ones with map and spread").toEqual([{ id: "DC-1042", qty: 2 }]);
  });
});
