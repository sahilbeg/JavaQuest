import { describe, it, expect } from "vitest";
import { parseCode, priceOf, parseMode, priceBoard, codesUpTo } from "../src/catalogue.ts";

describe("Fixed choices with as const", () => {
  it("reads an item code that someone typed", () => {
    expect(parseCode("BAG"), "\"BAG\" is a known code").toBe("BAG");
    expect(parseCode("bag"), "\"bag\" should be upper-cased to \"BAG\"").toBe("BAG");
    expect(parseCode(" cst "), "\" cst \" should be trimmed and upper-cased to \"CST\"").toBe("CST");
    expect(parseCode("XYZ"), "\"XYZ\" isn't one of Dhaaga's codes, so null").toBe(null);
    expect(parseCode(""), "an empty box is no code, so null").toBe(null);
  });

  it("looks up the price of a code", () => {
    expect(priceOf("AMI"), "an amigurumi costs 45000 paise").toBe(45000);
    expect(priceOf("KEY"), "a keychain costs 12000 paise").toBe(12000);
    expect(priceOf("BTY"), "baby booties cost 35000 paise").toBe(35000);
  });

  it("reads a payment mode that someone typed", () => {
    expect(parseMode("upi"), "\"upi\" should become \"UPI\"").toBe("UPI");
    expect(parseMode(" Card "), "\" Card \" should become \"CARD\"").toBe("CARD");
    expect(parseMode("GPay"), "\"GPay\" isn't one of the three modes, so null").toBe(null);
  });

  it("writes the price board in the order of ITEM_CODES", () => {
    expect(priceBoard(), "one line per code, with the price in rupees and two decimals").toEqual([
      "CST ₹150.00",
      "KEY ₹120.00",
      "AMI ₹450.00",
      "BAG ₹899.00",
      "BTY ₹350.00",
    ]);
  });

  it("lists the codes that fit a budget", () => {
    expect(codesUpTo(35000), "₹350 buys a coaster set, a keychain or booties, in that order").toEqual(["CST", "KEY", "BTY"]);
    expect(codesUpTo(89900), "₹899 buys anything").toEqual(["CST", "KEY", "AMI", "BAG", "BTY"]);
    expect(codesUpTo(10000), "₹100 buys nothing").toEqual([]);
  });
});
