import { describe, it, expect } from "vitest";
import { PRICES, toPaise, cartTotal } from "../src/cart.js";

const kapoorOrder = [
  { code: "BAG", qty: 2 },
  { code: "BTY", qty: 1 },
  { code: "CST", qty: 1 },
];

describe("INC-03 bronze: whole paise, not rupee decimals", () => {
  it("stores every price in whole paise", () => {
    expect(PRICES.CST, "a coaster set is ₹150, so PRICES.CST should be 15000 paise").toBe(15000);
    expect(PRICES.KEY, "a keychain is ₹120, so PRICES.KEY should be 12000 paise").toBe(12000);
    expect(PRICES.AMI, "an amigurumi is ₹450, so PRICES.AMI should be 45000 paise").toBe(45000);
    expect(PRICES.BAG, "a tote bag is ₹899, so PRICES.BAG should be 89900 paise").toBe(89900);
    expect(PRICES.BTY, "baby booties are ₹350, so PRICES.BTY should be 35000 paise").toBe(35000);
  });

  it("turns rupees from the old sheet into the nearest whole paisa", () => {
    expect(toPaise(899), "₹899 is 89900 paise").toBe(89900);
    expect(toPaise(2068.2), "₹2,068.20 is 206820 paise, not 206819").toBe(206820);
    expect(toPaise(0.29), "₹0.29 is 29 paise, even though 0.29 * 100 is 28.999999999999996").toBe(29);
    expect(toPaise(0.1 + 0.2), "0.1 + 0.2 rupees is 30 paise, even though the sum is 0.30000000000000004").toBe(30);
  });

  it("totals a small cart to the exact paisa", () => {
    expect(
      cartTotal([
        { code: "CST", qty: 2 },
        { code: "KEY", qty: 1 },
      ]),
      "two coaster sets and a keychain cost 30000 + 12000 = 42000 paise",
    ).toBe(42000);
  });

  it("totals Mrs. Kapoor's Diwali order to the exact paisa", () => {
    expect(
      cartTotal(kapoorOrder),
      "two tote bags, baby booties and a coaster set are 229800 paise. 10% off is 22980, which leaves 206820",
    ).toBe(206820);
  });
});
