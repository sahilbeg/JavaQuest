import { describe, it, expect } from "vitest";
import { PRICES, priceOf } from "../src/prices.js";
import * as pricesModule from "../src/prices.js";

describe("prices.js", () => {
  it("still exports the price list", () => {
    expect(PRICES.BAG, "the tote bag costs 89900 paise").toBe(89900);
  });

  it("exports DELIVERY_PAISE as 6000", () => {
    expect(pricesModule.DELIVERY_PAISE, "prices.js should have a named export DELIVERY_PAISE = 6000").toBe(6000);
  });

  it("looks up a price by code", () => {
    expect(priceOf("CST"), "a coaster set costs 15000 paise").toBe(15000);
    expect(priceOf("BTY"), "baby booties cost 35000 paise").toBe(35000);
  });

  it("gives 0 for a code that isn't on the list", () => {
    expect(priceOf("XYZ"), "an unknown code should cost 0, not undefined").toBe(0);
  });
});
