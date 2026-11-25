import { describe, it, expect } from "vitest";
import { lineTotal, withExtras, splitBill, sumAll, applyOffer } from "../src/pricing.ts";

describe("Typed pricing helpers", () => {
  it("multiplies one item's price by the quantity", () => {
    expect(lineTotal(15000, 2), "two coaster sets at 15000 paise cost 30000 paise").toBe(30000);
    expect(lineTotal(89900, 3), "three tote bags at 89900 paise cost 269700 paise").toBe(269700);
    expect(lineTotal(12000, 0), "zero keychains cost 0 paise").toBe(0);
  });

  it("leaves the price alone when no extras are asked for", () => {
    expect(withExtras(35000), "baby booties with no extras stay at 35000 paise").toBe(35000);
    expect(withExtras(35000, false), "giftWrap false adds nothing, so 35000 paise").toBe(35000);
  });

  it("adds gift wrap and a stitched name when asked for", () => {
    expect(withExtras(35000, true), "booties with gift wrap are 35000 + 3000 = 38000 paise").toBe(38000);
    expect(withExtras(12000, false, "Riya"), "a keychain with Riya stitched on is 12000 + 5000 = 17000 paise").toBe(17000);
    expect(withExtras(12000, true, "Riya"), "a wrapped keychain with a name is 12000 + 3000 + 5000 = 20000 paise").toBe(20000);
    expect(withExtras(12000, true, ""), "an empty name means no stitching, so only the wrap is added: 15000 paise").toBe(15000);
  });

  it("splits a bill, between 2 people when nobody says how many", () => {
    expect(splitBill(100000, 3), "₹1,000 among 3 friends is 33333 paise each, rounded down").toBe(33333);
    expect(splitBill(45000, 4), "₹450 among 4 people is 11250 paise each").toBe(11250);
    expect(splitBill(89900), "with no number of people, a tote bag is split between 2: 44950 paise each").toBe(44950);
  });

  it("adds up any number of amounts", () => {
    expect(sumAll(15000, 12000, 45000), "a coaster set, a keychain and an amigurumi make 72000 paise").toBe(72000);
    expect(sumAll(89900), "one amount on its own is just that amount").toBe(89900);
    expect(sumAll(), "with no amounts at all, the sum is 0").toBe(0);
  });

  it("takes off what the offer function says", () => {
    const tenPercent = (paise: number) => Math.floor(paise / 10);
    expect(applyOffer(200000, tenPercent), "10% off 200000 paise leaves 180000 paise").toBe(180000);
    expect(applyOffer(15000, () => 5000), "₹50 off a coaster set leaves 10000 paise").toBe(10000);
  });

  it("calls the offer with the price and never goes below zero", () => {
    const seen: number[] = [];
    const result = applyOffer(45000, (paise: number) => {
      seen.push(paise);
      return 0;
    });
    expect(seen, "the offer function should be called once, with the price 45000").toEqual([45000]);
    expect(result, "an offer of 0 paise leaves the price at 45000").toBe(45000);
    expect(applyOffer(3000, () => 5000), "₹50 off a ₹30 item can't go below 0 paise").toBe(0);
  });
});
