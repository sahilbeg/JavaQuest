import { describe, it, expect } from "vitest";
import {
  formatRupees,
  itemsTotal,
  extrasTotal,
  diwaliDiscount,
  deliveryFee,
  quote,
  receipt,
} from "../src/calculator.js";

// Two tote bags, and baby booties in gift wrap: big enough for the Diwali offer and free delivery.
const diwaliOrder = [
  { code: "BAG", qty: 2 },
  { code: "BTY", qty: 1, giftWrap: true },
];

// A coaster set, and two keychains with "Riya" stitched on: no offer, and delivery is charged.
const smallOrder = [
  { code: "CST", qty: 1 },
  { code: "KEY", qty: 2, name: "Riya" },
];

// A tote bag, and an amigurumi wrapped with "Golu" stitched on: free delivery, but no offer.
const middleOrder = [
  { code: "BAG", qty: 1 },
  { code: "AMI", qty: 1, giftWrap: true, name: "Golu" },
];

describe("The price and delivery calculator", () => {
  it("formats paise as Indian rupees", () => {
    expect(formatRupees(129900), '129900 paise should show as "₹1,299.00"').toBe("₹1,299.00");
    expect(formatRupees(6000), '6000 paise should show as "₹60.00"').toBe("₹60.00");
    expect(formatRupees(21780), '21780 paise should show as "₹217.80"').toBe("₹217.80");
  });

  it("adds up the items, without extras", () => {
    expect(itemsTotal(diwaliOrder), "two tote bags and baby booties cost 179800 + 35000 = 214800 paise").toBe(214800);
    expect(itemsTotal(smallOrder), "a coaster set and two keychains cost 15000 + 24000 = 39000 paise").toBe(39000);
    expect(itemsTotal([]), "an empty cart costs 0 paise").toBe(0);
  });

  it("adds gift wrap and stitched names for every item", () => {
    expect(extrasTotal(diwaliOrder), "one pair of booties in gift wrap adds 3000 paise").toBe(3000);
    expect(extrasTotal(smallOrder), "a name on each of two keychains adds 2 x 5000 = 10000 paise").toBe(10000);
    expect(
      extrasTotal([{ code: "AMI", qty: 3, giftWrap: true, name: "Golu" }]),
      "three wrapped amigurumi with a name each add 3 x 8000 = 24000 paise",
    ).toBe(24000);
    expect(extrasTotal([{ code: "CST", qty: 1, name: "" }]), "an empty name means nothing is stitched").toBe(0);
    expect(extrasTotal([{ code: "CST", qty: 1, giftWrap: false }]), "giftWrap: false adds nothing").toBe(0);
  });

  it("gives the Diwali offer only above ₹1,500, rounded down", () => {
    expect(diwaliDiscount(150000), "exactly ₹1,500 gets no offer").toBe(0);
    expect(diwaliDiscount(150001), "10% of 150001 is 15000.1, rounded down to 15000").toBe(15000);
    expect(diwaliDiscount(150019), "10% of 150019 is 15001.9, rounded down to 15001").toBe(15001);
    expect(diwaliDiscount(217800), "10% of 217800 is 21780").toBe(21780);
  });

  it("charges ₹60 delivery unless the subtotal is above ₹999", () => {
    expect(deliveryFee(49000), "a ₹490 order pays 6000 paise for delivery").toBe(6000);
    expect(deliveryFee(99900), "exactly ₹999 still pays 6000 paise").toBe(6000);
    expect(deliveryFee(99901), "above ₹999, delivery is free").toBe(0);
  });

  it("puts the whole quote together", () => {
    expect(quote(diwaliOrder), "the Diwali order: 217800 subtotal, 21780 off, free delivery, 196020 to pay").toEqual({
      items: 214800,
      extras: 3000,
      subtotal: 217800,
      discount: 21780,
      delivery: 0,
      total: 196020,
    });
    expect(quote(smallOrder), "the small order: 49000 subtotal, no offer, 6000 delivery, 55000 to pay").toEqual({
      items: 39000,
      extras: 10000,
      subtotal: 49000,
      discount: 0,
      delivery: 6000,
      total: 55000,
    });
    expect(quote(middleOrder), "the middle order: 142900 subtotal, no offer, free delivery").toEqual({
      items: 134900,
      extras: 8000,
      subtotal: 142900,
      discount: 0,
      delivery: 0,
      total: 142900,
    });
  });

  it("writes the receipt for an order with the Diwali offer", () => {
    const expected = [
      "Dhaaga & Co. price quote",
      "BAG x 2: ₹1,798.00",
      "BTY x 1: ₹350.00",
      "Extras: ₹30.00",
      "Subtotal: ₹2,178.00",
      "Diwali offer: -₹217.80",
      "Delivery: FREE",
      "Total: ₹1,960.20",
    ].join("\n");
    expect(receipt(diwaliOrder), "the receipt should match the KB page row for row, with no newline at the end").toBe(expected);
  });

  it("writes the receipt for a small order, with delivery and no offer row", () => {
    const expected = [
      "Dhaaga & Co. price quote",
      "CST x 1: ₹150.00",
      "KEY x 2: ₹240.00",
      "Extras: ₹100.00",
      "Subtotal: ₹490.00",
      "Delivery: ₹60.00",
      "Total: ₹550.00",
    ].join("\n");
    expect(receipt(smallOrder), "a small order has a ₹60.00 delivery row and no Diwali offer row").toBe(expected);
  });

  it("writes the receipt for an order with free delivery but no offer", () => {
    const expected = [
      "Dhaaga & Co. price quote",
      "BAG x 1: ₹899.00",
      "AMI x 1: ₹450.00",
      "Extras: ₹80.00",
      "Subtotal: ₹1,429.00",
      "Delivery: FREE",
      "Total: ₹1,429.00",
    ].join("\n");
    expect(receipt(middleOrder), "above ₹999 but not above ₹1,500: free delivery and no offer row").toBe(expected);
  });
});
