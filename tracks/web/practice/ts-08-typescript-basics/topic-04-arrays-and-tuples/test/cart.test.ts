import { describe, it, expect } from "vitest";
import { PRICE_LIST, priceOf, cartTotal, codesInCart, priceRange, cheapestFirst } from "../src/cart.ts";

describe("The cart with arrays and tuples", () => {
  it("looks up a price in the price list", () => {
    expect(priceOf("BAG"), "a tote bag (BAG) costs 89900 paise").toBe(89900);
    expect(priceOf("KEY"), "a keychain (KEY) costs 12000 paise").toBe(12000);
    expect(priceOf("CST"), "a coaster set (CST) costs 15000 paise").toBe(15000);
    expect(priceOf("XYZ"), "a code that isn't on the list gives 0").toBe(0);
  });

  it("totals a cart of [code, qty] lines", () => {
    expect(cartTotal([["CST", 2], ["KEY", 1]]), "two coaster sets and a keychain cost 30000 + 12000 = 42000 paise").toBe(42000);
    expect(cartTotal([["BAG", 1], ["ZZZ", 3]]), "an unknown code adds nothing, so only the tote bag counts: 89900 paise").toBe(89900);
    expect(cartTotal([]), "an empty cart costs 0 paise").toBe(0);
  });

  it("lists each code in the cart once, in the order it first appears", () => {
    expect(codesInCart([["KEY", 1], ["CST", 2], ["KEY", 3]]), "KEY appears twice but is listed once, before CST").toEqual(["KEY", "CST"]);
    expect(codesInCart([]), "an empty cart has no codes").toEqual([]);
  });

  it("finds the lowest and highest price as a pair", () => {
    expect(priceRange([45000, 12000, 89900]), "the cheapest is 12000 and the dearest is 89900").toEqual([12000, 89900]);
    expect(priceRange([35000]), "with one price, it is both the lowest and the highest").toEqual([35000, 35000]);
    expect(priceRange([]), "with no prices, the range is [0, 0]").toEqual([0, 0]);
  });

  it("sorts the price list from cheapest to dearest, without changing the original", () => {
    const sorted = cheapestFirst(PRICE_LIST);
    expect(sorted.map((entry) => entry[0]), "from cheapest: KEY, CST, BTY, AMI, BAG").toEqual(["KEY", "CST", "BTY", "AMI", "BAG"]);
    expect(sorted[0], "the first entry should be the whole tuple [\"KEY\", 12000]").toEqual(["KEY", 12000]);
    expect(PRICE_LIST[0][0], "the original PRICE_LIST must not be reordered: it still starts with CST").toBe("CST");
    expect(sorted === PRICE_LIST, "cheapestFirst should return a new array, not the one it was given").toBe(false);
  });
});
