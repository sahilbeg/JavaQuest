import { describe, it, expect } from "vitest";
import { last, uniqueBy, groupBy, paginate } from "../src/collections.ts";

const items = [
  { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000 },
  { id: "DC-1043", code: "CST", name: "Marigold coaster set", colour: "Marigold", paise: 15000 },
  { id: "DC-1044", code: "KEY", name: "Mint keychain", colour: "Mint", paise: 12000 },
  { id: "DC-1045", code: "BAG", name: "Indigo tote bag", colour: "Indigo", paise: 89900 },
  { id: "DC-1046", code: "AMI", name: "Rani pink parrot", colour: "Rani pink", paise: 45000 },
];

describe("last", () => {
  it("gives the last item of a list of items", () => {
    expect(last(items)?.id, "the last item in the list is DC-1046, the parrot").toBe("DC-1046");
  });

  it("works for a list of numbers too", () => {
    expect(last([15000, 12000, 45000]), "the last price in [15000, 12000, 45000] is 45000").toBe(45000);
  });

  it("gives undefined for an empty list", () => {
    expect(last([]), "an empty list has no last element, so last should give undefined").toBeUndefined();
  });
});

describe("uniqueBy", () => {
  it("keeps one item per code, the first one seen", () => {
    const ids = uniqueBy(items, (item) => item.code).map((item) => item.id);
    expect(ids, "DC-1046 is a second AMI, so only the first four items stay").toEqual(["DC-1042", "DC-1043", "DC-1044", "DC-1045"]);
  });

  it("works on a list of colours", () => {
    const colours = uniqueBy(["Mint", "Indigo", "Mint", "Marigold", "Indigo"], (colour) => colour);
    expect(colours, "each colour should appear once, in the order first seen").toEqual(["Mint", "Indigo", "Marigold"]);
  });

  it("doesn't change the list it was given", () => {
    const before = items.map((item) => item.id);
    uniqueBy(items, (item) => item.code);
    expect(items.map((item) => item.id), "uniqueBy should build a new list and leave the catalogue alone").toEqual(before);
  });
});

describe("groupBy", () => {
  it("puts items in groups by colour", () => {
    const groups = groupBy(items, (item) => item.colour);
    expect(groups.get("Indigo")?.map((item) => item.id), "Golu and the tote bag are both Indigo").toEqual(["DC-1042", "DC-1045"]);
    expect(groups.get("Mint")?.length, "one item is Mint").toBe(1);
  });

  it("keeps the keys in the order they first appear", () => {
    const groups = groupBy(items, (item) => item.colour);
    expect([...groups.keys()], "the colours should come in the order they first appear in the list").toEqual(["Indigo", "Marigold", "Mint", "Rani pink"]);
  });

  it("gives an empty Map for an empty list", () => {
    expect(groupBy([], (item: string) => item).size, "an empty list makes no groups").toBe(0);
  });
});

describe("paginate", () => {
  it("gives the first page of two items", () => {
    const first = paginate(items, 1, 2);
    expect(first.items.map((item) => item.id), "page 1 with 2 per page holds the first two items").toEqual(["DC-1042", "DC-1043"]);
    expect(first.page, "the page number should be 1").toBe(1);
    expect(first.totalPages, "5 items at 2 per page make 3 pages").toBe(3);
  });

  it("gives the last, shorter page", () => {
    const third = paginate(items, 3, 2);
    expect(third.items.map((item) => item.id), "page 3 holds only the fifth item").toEqual(["DC-1046"]);
  });

  it("gives no items for a page past the end", () => {
    const fourth = paginate(items, 4, 2);
    expect(fourth.items, "there is no page 4, so it has no items").toEqual([]);
    expect(fourth.totalPages, "there are still 3 pages in all").toBe(3);
  });

  it("counts one page for an empty list", () => {
    expect(paginate([], 1, 10).totalPages, "an empty catalogue still shows one (empty) page").toBe(1);
  });
});
