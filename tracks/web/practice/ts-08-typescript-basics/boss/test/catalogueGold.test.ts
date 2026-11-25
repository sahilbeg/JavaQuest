import { describe, it, expect } from "vitest";
import { rupeesToPaise } from "../src/catalogue.ts";

describe("INC-08 gold: the hand-typed rupee column", () => {
  it("reads plain rupee amounts", () => {
    expect(rupeesToPaise("450"), "\"450\" rupees is 45000 paise").toBe(45000);
    expect(rupeesToPaise("0.29"), "\"0.29\" rupees is 29 paise, rounded to the nearest paisa").toBe(29);
  });

  it("ignores the ₹ sign, commas and spaces", () => {
    expect(rupeesToPaise("₹1,299.50"), "\"₹1,299.50\" is 129950 paise").toBe(129950);
    expect(rupeesToPaise(" ₹ 899 "), "\" ₹ 899 \" is 89900 paise").toBe(89900);
    expect(rupeesToPaise("₹1,00,000"), "\"₹1,00,000\" (one lakh) is 10000000 paise").toBe(10000000);
  });

  it("refuses text that isn't an amount", () => {
    expect(() => rupeesToPaise("₹"), "a ₹ sign with no amount should throw \"Bad price\"").toThrow("Bad price");
    expect(() => rupeesToPaise("abc"), "\"abc\" should throw \"Bad price\"").toThrow("Bad price");
    expect(() => rupeesToPaise(""), "an empty cell should throw \"Bad price\"").toThrow("Bad price");
    expect(() => rupeesToPaise("-50"), "a negative amount should throw \"Bad price\"").toThrow("Bad price");
  });
});
