import { describe, it, expect } from "vitest";
import { isFreeDelivery, isWholePaise, sameQuantity, isBlankNote } from "../src/checks.js";

describe("Checkout checks", () => {
  it("gives free delivery only above ₹999", () => {
    expect(isFreeDelivery(99901), "99901 paise is above ₹999, so delivery is free").toBe(true);
    expect(isFreeDelivery(150000), "₹1,500 is above ₹999, so delivery is free").toBe(true);
    expect(isFreeDelivery(99900), "exactly ₹999 is not above ₹999, so delivery is not free").toBe(false);
    expect(isFreeDelivery(15000), "₹150 is well below ₹999, so delivery is not free").toBe(false);
  });

  it("accepts only whole numbers as paise", () => {
    expect(isWholePaise(15000), "15000 is a whole number of paise").toBe(true);
    expect(isWholePaise(0), "0 is a whole number too").toBe(true);
    expect(isWholePaise(150.5), "150.5 has half a paisa in it, so it is not whole paise").toBe(false);
    expect(isWholePaise("15000"), 'the text "15000" is a string, not a number').toBe(false);
    expect(isWholePaise(NaN), "NaN is not a whole number").toBe(false);
  });

  it("compares a typed quantity with the expected number", () => {
    expect(sameQuantity("2", 2), '"2" typed in a form means the quantity 2').toBe(true);
    expect(sameQuantity("3", 3), '"3" typed in a form means the quantity 3').toBe(true);
    expect(sameQuantity("2", 3), '"2" is not the quantity 3').toBe(false);
    expect(sameQuantity("two", 2), '"two" is not a number, so it can\'t match 2').toBe(false);
  });

  it("spots a gift note that is blank", () => {
    expect(isBlankNote(""), "an empty note is blank").toBe(true);
    expect(isBlankNote("   "), "a note of only spaces is blank").toBe(true);
    expect(isBlankNote("Happy birthday, Aai!"), "a note with words in it is not blank").toBe(false);
  });
});
