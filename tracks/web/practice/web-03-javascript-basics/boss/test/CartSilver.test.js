import { describe, it, expect } from "vitest";
import { diwaliDiscount, cartTotal } from "../src/cart.js";

describe("INC-03 silver: the Diwali offer", () => {
  it("gives no offer at exactly ₹1,500 or below", () => {
    expect(diwaliDiscount(150000), "exactly ₹1,500 is not above ₹1,500, so the discount is 0").toBe(0);
    expect(diwaliDiscount(99900), "a ₹999 order gets no Diwali discount").toBe(0);
  });

  it("takes 10% off orders above ₹1,500", () => {
    expect(diwaliDiscount(229800), "10% of 229800 paise is 22980 paise").toBe(22980);
    expect(diwaliDiscount(200000), "10% of 200000 paise is 20000 paise").toBe(20000);
  });

  it("rounds the discount down to whole paise", () => {
    expect(diwaliDiscount(150005), "10% of 150005 is 15000.5, which rounds down to 15000").toBe(15000);
    expect(diwaliDiscount(150019), "10% of 150019 is 15001.9, which rounds down to 15001").toBe(15001);
    expect(diwaliDiscount(150001), "10% of 150001 is 15000.1, which rounds down to 15000").toBe(15000);
  });

  it("charges full price for a cart of exactly ₹1,500", () => {
    expect(
      cartTotal([
        { code: "AMI", qty: 3 },
        { code: "CST", qty: 1 },
      ]),
      "three amigurumi and a coaster set are exactly 150000 paise, with no offer",
    ).toBe(150000);
  });

  it("takes the offer off a cart above ₹1,500", () => {
    expect(
      cartTotal([
        { code: "AMI", qty: 3 },
        { code: "CST", qty: 1 },
        { code: "KEY", qty: 1 },
      ]),
      "162000 paise minus the 16200 paise offer leaves 145800",
    ).toBe(145800);
  });
});
