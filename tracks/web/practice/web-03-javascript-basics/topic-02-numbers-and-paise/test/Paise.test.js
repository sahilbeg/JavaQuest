import { describe, it, expect } from "vitest";
import { lineTotal, wholeRupees, leftoverPaise, shareEach } from "../src/paise.js";

describe("Paise maths", () => {
  it("multiplies one item's price by the quantity", () => {
    expect(lineTotal(15000, 2), "two coaster sets at 15000 paise cost 30000 paise").toBe(30000);
    expect(lineTotal(89900, 3), "three tote bags at 89900 paise cost 269700 paise").toBe(269700);
    expect(lineTotal(12000, 0), "zero keychains cost 0 paise").toBe(0);
  });

  it("finds the whole rupees in an amount of paise", () => {
    expect(wholeRupees(15050), "15050 paise holds 150 whole rupees").toBe(150);
    expect(wholeRupees(89900), "89900 paise is exactly 899 rupees").toBe(899);
    expect(wholeRupees(99), "99 paise is less than one rupee, so 0 whole rupees").toBe(0);
  });

  it("finds the paise left over after the whole rupees", () => {
    expect(leftoverPaise(15050), "15050 paise has 50 paise left over after 150 rupees").toBe(50);
    expect(leftoverPaise(89900), "89900 paise is whole rupees, so 0 paise are left over").toBe(0);
    expect(leftoverPaise(99), "99 paise is all left over").toBe(99);
  });

  it("splits a bill equally, rounded down to whole paise", () => {
    expect(shareEach(100000, 3), "₹1,000 among 3 friends is 33333 paise each, rounded down").toBe(33333);
    expect(shareEach(45000, 2), "₹450 between 2 people is 22500 paise each").toBe(22500);
    expect(shareEach(200000, 3), "₹2,000 among 3 people is 66666 paise each, rounded down, not up").toBe(66666);
    expect(Number.isInteger(shareEach(15000, 7)), "a share must be a whole number of paise").toBe(true);
  });
});
