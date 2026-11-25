import { describe, it, expect } from "vitest";
import { toPaise, cartTotal } from "../src/catalogue.ts";

describe("INC-08 silver: bad prices and unknown items stop the sale", () => {
  it("refuses text that isn't a whole number of paise", () => {
    expect(() => toPaise("abc"), "\"abc\" is not a price, so toPaise should throw an Error saying \"Bad price\"").toThrow("Bad price");
    expect(() => toPaise("120.50"), "\"120.50\" isn't whole paise, so toPaise should throw \"Bad price\"").toThrow("Bad price");
    expect(() => toPaise("-100"), "a negative price should throw \"Bad price\"").toThrow("Bad price");
  });

  it("refuses an empty price instead of treating it as 0", () => {
    expect(() => toPaise(""), "an empty cell in the sheet should throw \"Bad price\", not count as 0").toThrow("Bad price");
    expect(() => toPaise("   "), "a cell of only spaces should throw \"Bad price\"").toThrow("Bad price");
  });

  it("refuses a number that isn't whole paise", () => {
    expect(() => toPaise(150.5), "150.5 paise can't be paid, so toPaise should throw \"Bad price\"").toThrow("Bad price");
  });

  it("stops a cart with a mistyped price in the sheet", () => {
    const rows = [{ code: "AMI", name: "Amigurumi", price: "45O00" }];
    expect(() => cartTotal([{ code: "AMI", qty: 1 }], rows), "\"45O00\" has a letter O in it, so the total should throw \"Bad price\"").toThrow(
      "Bad price",
    );
  });

  it("stops a cart with an item that isn't in the catalogue", () => {
    expect(() => cartTotal([{ code: "XYZ", qty: 1 }]), "an unknown code should throw \"Unknown item: XYZ\", not cost 0").toThrow(
      "Unknown item: XYZ",
    );
  });
});
