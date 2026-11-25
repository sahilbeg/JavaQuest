import { describe, it, expect } from "vitest";
import { queryCatalogue, shopHref } from "../src/lib/query.js";
import { getItems } from "../src/lib/catalogue.js";

const ids = (items) => items.map((item) => item.id);

describe("queryCatalogue", () => {
  it("keeps every item, in catalogue order, when nothing is chosen", async () => {
    const all = await getItems();
    expect(ids(queryCatalogue(all, {})), "with no choices, all 6 items should come back in catalogue order").toEqual([
      "DC-1038",
      "DC-1042",
      "DC-1043",
      "DC-1044",
      "DC-1045",
      "DC-1046",
    ]);
  });

  it("keeps only the chosen colour, using its slug", async () => {
    const all = await getItems();
    expect(ids(queryCatalogue(all, { colour: "indigo" })), "colour indigo should keep Golu and the tote bag").toEqual([
      "DC-1042",
      "DC-1045",
    ]);
    expect(ids(queryCatalogue(all, { colour: "rani-pink" })), "the slug rani-pink should find items whose colour is Rani pink").toEqual([
      "DC-1038",
      "DC-1046",
    ]);
  });

  it("gives nothing for a colour that doesn't exist", async () => {
    const all = await getItems();
    expect(queryCatalogue(all, { colour: "purple" }), "an unknown colour slug should give an empty list").toEqual([]);
  });

  it("searches names, ignoring case and spaces around the search", async () => {
    const all = await getItems();
    expect(ids(queryCatalogue(all, { q: "ELEPHANT" })), "searching ELEPHANT should find both elephants").toEqual(["DC-1038", "DC-1042"]);
    expect(ids(queryCatalogue(all, { q: "  tote " })), "searching \"  tote \" should find the tote bag").toEqual(["DC-1045"]);
  });

  it("sorts cheapest first or dearest first", async () => {
    const all = await getItems();
    expect(
      ids(queryCatalogue(all, { colour: "rani-pink", sort: "price-asc" })),
      "Rani pink, cheapest first: the ₹350.00 booties, then the ₹450.00 elephant",
    ).toEqual(["DC-1046", "DC-1038"]);
    expect(
      ids(queryCatalogue(all, { sort: "price-desc" })),
      "dearest first, with the two ₹450.00 elephants in catalogue order",
    ).toEqual(["DC-1045", "DC-1038", "DC-1042", "DC-1046", "DC-1043", "DC-1044"]);
  });

  it("never changes the list it was given", async () => {
    const all = await getItems();
    queryCatalogue(all, { sort: "price-asc" });
    expect(ids(all), "sorting should work on a copy, leaving the catalogue's own order alone").toEqual([
      "DC-1038",
      "DC-1042",
      "DC-1043",
      "DC-1044",
      "DC-1045",
      "DC-1046",
    ]);
  });
});

describe("shopHref", () => {
  it("is just /items with no choices", () => {
    expect(shopHref({}), "no choices should give /items").toBe("/items");
    expect(shopHref({ colour: "", q: "", sort: "" }), "empty choices should give /items").toBe("/items");
  });

  it("adds the non-empty choices in the order colour, q, sort", () => {
    expect(shopHref({ colour: "indigo" }), "colour indigo").toBe("/items?colour=indigo");
    expect(shopHref({ sort: "price-asc", q: "", colour: "indigo" }), "colour then sort, leaving out the empty q").toBe(
      "/items?colour=indigo&sort=price-asc",
    );
    expect(shopHref({ colour: "mint", q: "golu", sort: "price-desc" }), "all three choices").toBe(
      "/items?colour=mint&q=golu&sort=price-desc",
    );
  });

  it("encodes the search text", () => {
    expect(shopHref({ q: "golu elephant" }), "URLSearchParams writes a space as +").toBe("/items?q=golu+elephant");
  });
});
