import { describe, it, expect } from "vitest";
import { names, inStock, stockValue, findById, cheapestFirst } from "../src/catalogue.js";

function catalogue() {
  return [
    { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 },
    { id: "DC-1043", code: "CST", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 12 },
    { id: "DC-1044", code: "KEY", name: "Mint owl keychain", colour: "Mint", paise: 12000, stock: 0 },
    { id: "DC-1045", code: "BAG", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 },
    { id: "DC-1046", code: "BTY", name: "Rani pink baby booties", colour: "Rani pink", paise: 35000, stock: 4 },
    { id: "DC-1047", code: "AMI", name: "Off-white bunny", colour: "Off-white", paise: 45000, stock: 0 },
  ];
}

const ids = (items) => items.map((item) => item.id).join(",");

describe("names", () => {
  it("returns every name in catalogue order", () => {
    expect(names(catalogue()).join(" | "), "names should list all six items in order").toBe(
      "Golu the elephant | Marigold coaster set | Mint owl keychain | Indigo tote bag | Rani pink baby booties | Off-white bunny",
    );
  });

  it("returns an empty list for an empty catalogue", () => {
    expect(names([]).length, "no items means no names").toBe(0);
  });
});

describe("inStock", () => {
  it("keeps only the items with stock above 0", () => {
    expect(ids(inStock(catalogue())), "the owl keychain and the bunny have no stock, so they should be dropped").toBe(
      "DC-1042,DC-1043,DC-1045,DC-1046",
    );
  });

  it("leaves the catalogue it was given alone", () => {
    const list = catalogue();
    inStock(list);
    expect(list.length, "inStock must not remove items from the original list").toBe(6);
  });
});

describe("stockValue", () => {
  it("adds paise times stock for every item", () => {
    expect(stockValue(catalogue()), "135000 + 180000 + 0 + 449500 + 140000 + 0 is 904500 paise").toBe(904500);
  });

  it("is 0 for an empty catalogue", () => {
    expect(stockValue([]), "an empty list is worth 0, without crashing").toBe(0);
  });
});

describe("findById", () => {
  it("finds an item by its id", () => {
    const golu = findById(catalogue(), "DC-1042");
    expect(golu && golu.name, "DC-1042 is Golu the elephant").toBe("Golu the elephant");
    const bag = findById(catalogue(), "DC-1045");
    expect(bag && bag.code, "DC-1045 is the tote bag").toBe("BAG");
  });

  it("returns undefined when no item has that id", () => {
    expect(findById(catalogue(), "DC-9999"), "there is no DC-9999, so the answer should be undefined").toBeUndefined();
  });
});

describe("cheapestFirst", () => {
  it("sorts by price, cheapest first", () => {
    expect(ids(cheapestFirst(catalogue())), "keychain, coasters, booties, Golu, bunny, then the bag").toBe(
      "DC-1044,DC-1043,DC-1046,DC-1042,DC-1047,DC-1045",
    );
  });

  it("keeps the order it was given for items with the same price", () => {
    const sorted = cheapestFirst(catalogue());
    expect(sorted[3] && sorted[3].id, "Golu and the bunny both cost 45000, so Golu stays first").toBe("DC-1042");
  });

  it("does not reorder the list it was given", () => {
    const list = catalogue();
    cheapestFirst(list);
    expect(ids(list), "the original catalogue must keep its order: sort a copy").toBe(
      "DC-1042,DC-1043,DC-1044,DC-1045,DC-1046,DC-1047",
    );
  });
});
