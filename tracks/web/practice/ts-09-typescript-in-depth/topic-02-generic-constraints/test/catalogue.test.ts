import { describe, it, expect } from "vitest";
import { cheapest, pluck, findBy, linesTotal } from "../src/catalogue.ts";

const items = [
  { id: "DC-1042", code: "AMI", name: "Golu the elephant", paise: 45000, stock: 3 },
  { id: "DC-1043", code: "CST", name: "Marigold coaster set", paise: 15000, stock: 0 },
  { id: "DC-1044", code: "KEY", name: "Mint keychain", paise: 12000, stock: 8 },
  { id: "DC-1045", code: "BAG", name: "Indigo tote bag", paise: 89900, stock: 2 },
];

const bundles = [
  { title: "Diwali gift box", itemIds: ["DC-1042", "DC-1043"], paise: 55000 },
  { title: "Baby shower set", itemIds: ["DC-1047"], paise: 35000 },
];

describe("cheapest", () => {
  it("finds the cheapest item", () => {
    expect(cheapest(items)?.id, "the Mint keychain at ₹120.00 is the cheapest item").toBe("DC-1044");
  });

  it("works for gift bundles too, and keeps their fields", () => {
    expect(cheapest(bundles)?.title, "the ₹350.00 baby shower set is the cheapest bundle").toBe("Baby shower set");
  });

  it("keeps the first one on a tie", () => {
    const tie = [
      { code: "AMI", paise: 45000 },
      { code: "KEY", paise: 12000 },
      { code: "KEY", paise: 12000, note: "second" },
    ];
    expect(cheapest(tie), "with two keychains at the same price, the first should win").toBe(tie[1]);
  });

  it("gives undefined for an empty list", () => {
    expect(cheapest([]), "an empty list has no cheapest element").toBeUndefined();
  });
});

describe("pluck", () => {
  it("gives every item's id, in order", () => {
    expect(pluck(items, "id"), "pluck(items, \"id\") should list the four ids in order").toEqual(["DC-1042", "DC-1043", "DC-1044", "DC-1045"]);
  });

  it("gives every item's price", () => {
    expect(pluck(items, "paise"), "pluck(items, \"paise\") should list the four prices").toEqual([45000, 15000, 12000, 89900]);
  });

  it("works on bundles", () => {
    expect(pluck(bundles, "title"), "pluck(bundles, \"title\") should list both bundle names").toEqual(["Diwali gift box", "Baby shower set"]);
  });
});

describe("findBy", () => {
  it("finds the item with a code", () => {
    expect(findBy(items, "code", "BAG")?.id, "the BAG item is the Indigo tote bag, DC-1045").toBe("DC-1045");
  });

  it("finds by a number field too", () => {
    expect(findBy(items, "stock", 0)?.name, "the only item with 0 in stock is the coaster set").toBe("Marigold coaster set");
  });

  it("gives undefined when nothing matches", () => {
    expect(findBy(items, "code", "BTY"), "no item has the code BTY, so findBy should give undefined").toBeUndefined();
  });
});

describe("linesTotal", () => {
  it("adds paise times qty for every line", () => {
    const lines = [
      { code: "BAG", paise: 89900, qty: 2 },
      { code: "CST", paise: 15000, qty: 1 },
    ];
    expect(linesTotal(lines), "two tote bags and a coaster set come to 194800 paise").toBe(194800);
  });

  it("gives 0 for an empty cart", () => {
    expect(linesTotal([]), "no lines cost nothing").toBe(0);
  });
});
