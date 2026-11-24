import { describe, it, expect } from "vitest";
import { countWhere, applyToAll, priceAtMost, withGiftWrap } from "../src/offers.js";

const CATALOGUE = [
  { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 },
  { id: "DC-1043", code: "CST", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 12 },
  { id: "DC-1044", code: "KEY", name: "Mint owl keychain", colour: "Mint", paise: 12000, stock: 0 },
  { id: "DC-1045", code: "BAG", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 },
  { id: "DC-1046", code: "BTY", name: "Rani pink baby booties", colour: "Rani pink", paise: 35000, stock: 4 },
  { id: "DC-1047", code: "AMI", name: "Off-white bunny", colour: "Off-white", paise: 45000, stock: 0 },
];

describe("countWhere", () => {
  it("counts the items that pass the test", () => {
    expect(countWhere(CATALOGUE, (item) => item.colour === "Indigo"), "two items are Indigo").toBe(2);
    expect(countWhere(CATALOGUE, (item) => item.stock > 0), "four items are in stock").toBe(4);
  });

  it("returns 0 when nothing passes", () => {
    expect(countWhere(CATALOGUE, (item) => item.colour === "Black"), "no item is Black, so the count is 0").toBe(0);
  });

  it("returns 0 for an empty list", () => {
    expect(countWhere([], () => true), "an empty list has nothing to count").toBe(0);
  });
});

describe("applyToAll", () => {
  it("collects what the function returns for each item, in order", () => {
    const ids = applyToAll(CATALOGUE, (item) => item.id);
    expect(ids.join(","), "applyToAll with (item) => item.id should give every id in order").toBe(
      "DC-1042,DC-1043,DC-1044,DC-1045,DC-1046,DC-1047",
    );
  });

  it("returns a new array and leaves the old one alone", () => {
    const result = applyToAll(CATALOGUE, (item) => item.paise * 2);
    expect(result === CATALOGUE, "applyToAll should return a new array, not the one it was given").toBe(false);
    expect(result[0], "Golu doubled should be 90000").toBe(90000);
    expect(CATALOGUE[0].paise, "the catalogue's own price must not change").toBe(45000);
  });
});

describe("priceAtMost", () => {
  it("returns a function", () => {
    expect(typeof priceAtMost(35000), "priceAtMost should give back a function").toBe("function");
  });

  it("makes a test that accepts prices up to the limit", () => {
    const upTo450 = priceAtMost(45000);
    expect(upTo450(CATALOGUE[0]), "Golu costs exactly 45000, which is not above the limit").toBe(true);
    expect(upTo450(CATALOGUE[1]), "the coaster set costs 15000, under the limit").toBe(true);
    expect(upTo450(CATALOGUE[3]), "the tote bag costs 89900, above the limit").toBe(false);
  });

  it("works together with countWhere", () => {
    expect(countWhere(CATALOGUE, priceAtMost(35000)), "the coasters, the keychain and the booties cost ₹350 or less").toBe(3);
  });
});

describe("withGiftWrap", () => {
  it("adds ₹30 to the price the given function works out", () => {
    const wrapped = withGiftWrap((item) => item.paise);
    expect(wrapped(CATALOGUE[0]), "Golu with gift wrap should cost 45000 + 3000").toBe(48000);
  });

  it("uses whatever price function it was given", () => {
    const twoWrapped = withGiftWrap((item) => item.paise * 2);
    expect(twoWrapped(CATALOGUE[1]), "two coaster sets plus wrap should cost 30000 + 3000").toBe(33000);
  });
});
