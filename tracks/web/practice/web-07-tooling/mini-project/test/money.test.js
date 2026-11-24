import { describe, it, expect } from "vitest";
import { formatRupees, paiseFromRupees } from "../src/money.js";

describe("formatRupees", () => {
  it("shows whole paise as rupees with two decimals", () => {
    expect(formatRupees(15000), "a coaster set, 15000 paise, is ₹150.00").toBe("₹150.00");
    expect(formatRupees(0), "0 paise is ₹0.00").toBe("₹0.00");
    expect(formatRupees(5), "5 paise is ₹0.05").toBe("₹0.05");
  });

  it("groups thousands with a comma", () => {
    expect(formatRupees(129900), "129900 paise is ₹1,299.00").toBe("₹1,299.00");
  });

  it("groups lakhs the Indian way", () => {
    expect(formatRupees(15000000), "15000000 paise is one and a half lakh rupees, ₹1,50,000.00").toBe("₹1,50,000.00");
  });

  it("refuses anything that is not whole paise", () => {
    expect(() => formatRupees(12.5), "12.5 is not whole paise, so it should throw a TypeError").toThrow(TypeError);
    expect(() => formatRupees("15000"), "the text \"15000\" is not a number of paise").toThrow(TypeError);
  });
});

describe("paiseFromRupees", () => {
  it("reads plain rupees", () => {
    expect(paiseFromRupees("450"), "\"450\" is 45000 paise").toBe(45000);
  });

  it("accepts a ₹ sign, with or without a space", () => {
    expect(paiseFromRupees("₹450"), "\"₹450\" is 45000 paise").toBe(45000);
    expect(paiseFromRupees("₹ 450"), "\"₹ 450\" is 45000 paise").toBe(45000);
  });

  it("accepts commas and paise", () => {
    expect(paiseFromRupees("₹1,299.50"), "\"₹1,299.50\" is 129950 paise").toBe(129950);
    expect(paiseFromRupees("0.05"), "\"0.05\" is 5 paise").toBe(5);
  });

  it("reads one decimal digit as tens of paise", () => {
    expect(paiseFromRupees("1299.5"), "\"1299.5\" means ₹1,299.50, which is 129950 paise").toBe(129950);
  });

  it("ignores spaces around the amount", () => {
    expect(paiseFromRupees("  99.90 "), "\"  99.90 \" is 9990 paise").toBe(9990);
  });

  it("refuses text that is not an amount", () => {
    for (const bad of ["", "abc", "-5", "12.345", "₹", "1.2.3"]) {
      expect(() => paiseFromRupees(bad), `"${bad}" is not an amount, so it should throw a RangeError`).toThrow(
        RangeError,
      );
    }
  });

  it("refuses anything that is not text", () => {
    expect(() => paiseFromRupees(450), "the number 450 is not text, so it should throw a TypeError").toThrow(TypeError);
  });
});
