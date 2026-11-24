import { describe, it, expect } from "vitest";
import { deliveryFee, stallOpen, noteLine, priceBand } from "../src/rules.js";

describe("Website rules", () => {
  it("charges ₹60 delivery only when it should", () => {
    expect(deliveryFee(50000, false), "a ₹500 order delivered in Pune pays 6000 paise").toBe(6000);
    expect(deliveryFee(99900, false), "exactly ₹999 still pays 6000 paise for delivery").toBe(6000);
    expect(deliveryFee(99901, false), "above ₹999, delivery is free").toBe(0);
    expect(deliveryFee(50000, true), "a pickup at the stall never pays for delivery").toBe(0);
    expect(deliveryFee(120000, true), "a big order picked up at the stall pays nothing either").toBe(0);
  });

  it("knows when the FC Road stall is open", () => {
    expect(stallOpen("Sat", 10), "the stall opens at 10:00 on Saturday").toBe(true);
    expect(stallOpen("Sun", 19), "the stall is still open at 19:00 on Sunday").toBe(true);
    expect(stallOpen("Sun", 20), "the stall closes at 20:00").toBe(false);
    expect(stallOpen("Sat", 9), "the stall is not open yet at 9:00").toBe(false);
    expect(stallOpen("Mon", 12), "the stall is closed on Monday").toBe(false);
    expect(stallOpen("Fri", 15), "the stall is closed on Friday").toBe(false);
  });

  it("prints the gift note, or says there is none", () => {
    expect(noteLine("Happy Diwali!"), 'noteLine("Happy Diwali!") should be "Note: Happy Diwali!"').toBe("Note: Happy Diwali!");
    expect(noteLine(""), 'an empty note should give "No note"').toBe("No note");
    expect(noteLine(undefined), 'a missing note should give "No note", not "Note: undefined"').toBe("No note");
    expect(noteLine(null), 'a null note should give "No note"').toBe("No note");
  });

  it("puts each price in the right band", () => {
    expect(priceBand(12000), "a ₹120 keychain is under ₹200").toBe("under ₹200");
    expect(priceBand(15000), "a ₹150 coaster set is under ₹200").toBe("under ₹200");
    expect(priceBand(20000), "exactly ₹200 belongs to ₹200 to ₹499").toBe("₹200 to ₹499");
    expect(priceBand(45000), "a ₹450 amigurumi belongs to ₹200 to ₹499").toBe("₹200 to ₹499");
    expect(priceBand(50000), "exactly ₹500 belongs to ₹500 and up").toBe("₹500 and up");
    expect(priceBand(89900), "a ₹899 tote bag belongs to ₹500 and up").toBe("₹500 and up");
  });
});
