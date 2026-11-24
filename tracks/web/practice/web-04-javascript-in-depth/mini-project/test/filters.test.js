import { describe, it, expect } from "vitest";
import { filterCatalogue, colourOptions, priceRange } from "../src/filters.js";

function catalogue() {
  return [
    { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 },
    { id: "DC-1043", code: "CST", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 12 },
    { id: "DC-1044", code: "KEY", name: "Mint owl keychain", colour: "Mint", paise: 12000, stock: 0 },
    { id: "DC-1045", code: "BAG", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 },
    { id: "DC-1046", code: "BTY", name: "Rani pink baby booties", colour: "Rani pink", paise: 35000, stock: 4 },
    { id: "DC-1047", code: "AMI", name: "Off-white bunny", colour: "Off-white", paise: 45000, stock: 0 },
    { id: "DC-1048", code: "AMI", name: "Mint turtle", colour: "Mint", paise: 45000, stock: 2 },
    { id: "DC-1049", code: "CST", name: "Indigo coaster set", colour: "Indigo", paise: 15000, stock: 6 },
  ];
}

const ALL = "DC-1042,DC-1043,DC-1044,DC-1045,DC-1046,DC-1047,DC-1048,DC-1049";
const ids = (items) => items.map((item) => item.id).join(",");

describe("filterCatalogue with no options", () => {
  it("returns every item in catalogue order", () => {
    expect(ids(filterCatalogue(catalogue())), "with no options, all eight items should come back").toBe(ALL);
    expect(ids(filterCatalogue(catalogue(), {})), "an empty options object should filter nothing").toBe(ALL);
  });

  it("returns a new array", () => {
    const items = catalogue();
    expect(filterCatalogue(items) === items, "the result should be a new array, not the catalogue itself").toBe(false);
  });
});

describe("filterCatalogue by colour", () => {
  it("keeps only items of that colour", () => {
    expect(ids(filterCatalogue(catalogue(), { colour: "Indigo" })), "Golu, the tote bag and the Indigo coasters are Indigo").toBe(
      "DC-1042,DC-1045,DC-1049",
    );
  });

  it("ignores capital letters", () => {
    expect(ids(filterCatalogue(catalogue(), { colour: "rani PINK" })), "\"rani PINK\" should match Rani pink").toBe("DC-1046");
  });

  it("treats an empty colour as any colour", () => {
    expect(ids(filterCatalogue(catalogue(), { colour: "" })), "colour \"\" means no colour filter").toBe(ALL);
  });
});

describe("filterCatalogue by price", () => {
  it("keeps prices within the range, ends included", () => {
    expect(
      ids(filterCatalogue(catalogue(), { minPaise: 15000, maxPaise: 45000 })),
      "₹150 to ₹450 drops only the ₹120 keychain and the ₹899 bag",
    ).toBe("DC-1042,DC-1043,DC-1046,DC-1047,DC-1048,DC-1049");
  });

  it("works with only a maximum", () => {
    expect(ids(filterCatalogue(catalogue(), { maxPaise: 35000 })), "items up to ₹350").toBe("DC-1043,DC-1044,DC-1046,DC-1049");
  });

  it("works with only a minimum", () => {
    expect(ids(filterCatalogue(catalogue(), { minPaise: 45000 })), "items from ₹450 up").toBe("DC-1042,DC-1045,DC-1047,DC-1048");
  });

  it("treats 0 as a real limit", () => {
    expect(ids(filterCatalogue(catalogue(), { minPaise: 0 })), "a minimum of ₹0 lets everything through").toBe(ALL);
    expect(filterCatalogue(catalogue(), { maxPaise: 0 }).length, "a maximum of ₹0 lets nothing through").toBe(0);
  });
});

describe("filterCatalogue by stock", () => {
  it("hides sold-out items when inStockOnly is true", () => {
    expect(ids(filterCatalogue(catalogue(), { inStockOnly: true })), "the owl keychain and the bunny are sold out").toBe(
      "DC-1042,DC-1043,DC-1045,DC-1046,DC-1048,DC-1049",
    );
  });

  it("shows everything when inStockOnly is false", () => {
    expect(ids(filterCatalogue(catalogue(), { inStockOnly: false })), "inStockOnly false filters nothing").toBe(ALL);
  });
});

describe("filterCatalogue by search", () => {
  it("finds names that contain the text", () => {
    expect(ids(filterCatalogue(catalogue(), { search: "coaster" })), "two items have coaster in their name").toBe("DC-1043,DC-1049");
  });

  it("ignores capital letters and spaces around the text", () => {
    expect(ids(filterCatalogue(catalogue(), { search: "  GOLU " })), "\"  GOLU \" should find Golu the elephant").toBe("DC-1042");
  });

  it("treats empty or blank text as no search", () => {
    expect(ids(filterCatalogue(catalogue(), { search: "" })), "an empty search filters nothing").toBe(ALL);
    expect(ids(filterCatalogue(catalogue(), { search: "   " })), "a search of only spaces filters nothing").toBe(ALL);
  });
});

describe("filterCatalogue sorting", () => {
  it("sorts cheapest first, keeping catalogue order for equal prices", () => {
    expect(ids(filterCatalogue(catalogue(), { sort: "price-asc" })), "₹120, ₹150, ₹150, ₹350, ₹450 three times, then ₹899").toBe(
      "DC-1044,DC-1043,DC-1049,DC-1046,DC-1042,DC-1047,DC-1048,DC-1045",
    );
  });

  it("sorts dearest first, keeping catalogue order for equal prices", () => {
    expect(ids(filterCatalogue(catalogue(), { sort: "price-desc" })), "₹899 first, then the ₹450 items in catalogue order").toBe(
      "DC-1045,DC-1042,DC-1047,DC-1048,DC-1046,DC-1043,DC-1049,DC-1044",
    );
  });

  it("never reorders the catalogue it was given", () => {
    const items = catalogue();
    filterCatalogue(items, { sort: "price-desc" });
    expect(ids(items), "the catalogue passed in must keep its order").toBe(ALL);
  });
});

describe("filterCatalogue with several options", () => {
  it("answers: something in Indigo under ₹500, in stock, cheapest first", () => {
    const options = { colour: "Indigo", maxPaise: 50000, inStockOnly: true, sort: "price-asc" };
    expect(ids(filterCatalogue(catalogue(), options)), "the Indigo coasters (₹150), then Golu (₹450)").toBe("DC-1049,DC-1042");
  });

  it("combines a search with the stock switch", () => {
    expect(
      ids(filterCatalogue(catalogue(), { search: "mint", inStockOnly: true })),
      "the Mint owl keychain is sold out, so only the Mint turtle is left",
    ).toBe("DC-1048");
  });
});

describe("colourOptions", () => {
  it("lists each colour once, A to Z", () => {
    expect(colourOptions(catalogue()).join(", "), "five colours, Indigo only once, in alphabetical order").toBe(
      "Indigo, Marigold, Mint, Off-white, Rani pink",
    );
  });
});

describe("priceRange", () => {
  it("gives the cheapest and the dearest price", () => {
    expect(priceRange(catalogue()), "the keychain is ₹120 and the tote bag ₹899").toEqual({ minPaise: 12000, maxPaise: 89900 });
  });

  it("gives null for an empty list", () => {
    expect(priceRange([]), "an empty catalogue has no price range").toBeNull();
  });
});
