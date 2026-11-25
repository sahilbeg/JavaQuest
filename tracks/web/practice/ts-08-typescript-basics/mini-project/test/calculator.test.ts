import { describe, it, expect } from "vitest";
import { parseCode, itemsTotal, extrasTotal, diwaliDiscount, deliveryFee, quote } from "../src/calculator.ts";

const joshiLines = [
  { code: "BAG", qty: 2 },
  { code: "BTY", qty: 1, giftWrap: true },
];

describe("The typed price calculator", () => {
  it("reads an item code typed in a WhatsApp message", () => {
    expect(parseCode("bag"), "\"bag\" should become \"BAG\"").toBe("BAG");
    expect(parseCode(" Key "), "\" Key \" should become \"KEY\"").toBe("KEY");
    expect(parseCode("elephant"), "\"elephant\" isn't an item code, so null").toBe(null);
  });

  it("adds up price times quantity for the items", () => {
    expect(itemsTotal(joshiLines), "two tote bags and booties are 179800 + 35000 = 214800 paise").toBe(214800);
    expect(itemsTotal([]), "no lines cost 0 paise").toBe(0);
  });

  it("charges extras for every item that asks for them", () => {
    expect(extrasTotal(joshiLines), "one pair of wrapped booties is 3000 paise of extras").toBe(3000);
    expect(extrasTotal([{ code: "KEY", qty: 2, name: "Riya" }]), "two keychains with Riya stitched on are 2 x 5000 = 10000").toBe(10000);
    expect(extrasTotal([{ code: "AMI", qty: 1, giftWrap: true, name: "Golu" }]), "a wrapped and named toy is 3000 + 5000 = 8000").toBe(8000);
    expect(extrasTotal([{ code: "CST", qty: 3 }]), "no giftWrap and no name means no extras").toBe(0);
  });

  it("gives the Diwali offer only above ₹1,500, rounded down", () => {
    expect(diwaliDiscount(217800), "10% of 217800 is 21780").toBe(21780);
    expect(diwaliDiscount(150019), "10% of 150019 is 15001.9, rounded down to 15001").toBe(15001);
    expect(diwaliDiscount(150000), "exactly ₹1,500 is not above ₹1,500, so no offer").toBe(0);
  });

  it("charges delivery in Pune only up to ₹999, and never for a pickup", () => {
    expect(deliveryFee(49000, "pune"), "a ₹490 order delivered in Pune pays 6000").toBe(6000);
    expect(deliveryFee(99900, "pune"), "exactly ₹999 is not above ₹999, so it still pays 6000").toBe(6000);
    expect(deliveryFee(99901, "pune"), "above ₹999, delivery in Pune is free").toBe(0);
    expect(deliveryFee(49000, "pickup"), "a pickup from the stall never pays delivery").toBe(0);
  });

  it("builds the whole quote for Mrs. Joshi", () => {
    expect(quote({ lines: joshiLines, delivery: "pune", payment: "UPI" }), "items, extras, subtotal, offer, delivery and total, all in paise").toEqual({
      items: 214800,
      extras: 3000,
      subtotal: 217800,
      discount: 21780,
      delivery: 0,
      total: 196020,
    });
  });

  it("builds a small quote with delivery and no offer", () => {
    const lines = [
      { code: "CST", qty: 1 },
      { code: "KEY", qty: 2, name: "Riya" },
    ];
    expect(quote({ lines, delivery: "pune", payment: "CASH" }), "39000 of items and 10000 of extras, plus 6000 delivery: 55000").toEqual({
      items: 39000,
      extras: 10000,
      subtotal: 49000,
      discount: 0,
      delivery: 6000,
      total: 55000,
    });
  });
});
