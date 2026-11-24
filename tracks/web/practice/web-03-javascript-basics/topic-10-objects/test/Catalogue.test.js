import { describe, it, expect } from "vitest";
import { CATALOGUE, priceOf, makeLine, cartTotal } from "../src/catalogue.js";

describe("The catalogue object", () => {
  it("has the baby booties on the price list", () => {
    expect(CATALOGUE.BTY, 'CATALOGUE.BTY should be { name: "Baby booties", paise: 35000 }').toEqual({
      name: "Baby booties",
      paise: 35000,
    });
  });

  it("looks up the price of a code", () => {
    expect(priceOf("AMI"), "an amigurumi costs 45000 paise").toBe(45000);
    expect(priceOf("BAG"), "a tote bag costs 89900 paise").toBe(89900);
    expect(priceOf("BTY"), "baby booties cost 35000 paise").toBe(35000);
    expect(priceOf("XYZ"), "XYZ is not in the catalogue, so its price is null").toBe(null);
  });

  it("makes a cart line from a code and a quantity", () => {
    expect(makeLine("AMI", 2), "makeLine(\"AMI\", 2) should copy the name and price and add the qty").toEqual({
      code: "AMI",
      name: "Amigurumi",
      paise: 45000,
      qty: 2,
    });
    expect(makeLine("KEY", 1), "makeLine(\"KEY\", 1) should describe one keychain").toEqual({
      code: "KEY",
      name: "Keychain",
      paise: 12000,
      qty: 1,
    });
    expect(makeLine("XYZ", 1), "a code that is not in the catalogue should give null").toBe(null);
  });

  it("adds up a cart of line objects", () => {
    const lines = [
      { code: "CST", name: "Coaster set (4)", paise: 15000, qty: 2 },
      { code: "KEY", name: "Keychain", paise: 12000, qty: 1 },
    ];
    expect(cartTotal(lines), "two coaster sets and a keychain cost 30000 + 12000 = 42000 paise").toBe(42000);
    expect(cartTotal([]), "an empty cart costs 0 paise").toBe(0);
  });
});
