import { describe, it, expect } from "vitest";
import { deliveryFee, DELIVERY_PAISE, FREE_ABOVE_PAISE } from "../src/delivery.js";

describe("deliveryFee", () => {
  it("charges ₹60 on a ₹450 order", () => {
    expect(deliveryFee(45000), "one amigurumi (45000 paise) pays 6000 paise delivery").toBe(6000);
  });

  it("still charges ₹60 on exactly ₹999, because free delivery starts above ₹999", () => {
    expect(deliveryFee(99900), "99900 paise is not above ₹999, so delivery is 6000").toBe(6000);
  });

  it("is free one paisa above ₹999", () => {
    expect(deliveryFee(99901), "99901 paise is above ₹999, so delivery is free").toBe(0);
  });

  it("is free on a big order", () => {
    expect(deliveryFee(179800), "two tote bags (179800 paise) get free delivery").toBe(0);
  });

  it("charges nothing for an empty cart", () => {
    expect(deliveryFee(0), "there is nothing to deliver in an empty cart").toBe(0);
  });

  it("keeps the shared constants", () => {
    expect(DELIVERY_PAISE, "delivery inside Pune is ₹60, which is 6000 paise").toBe(6000);
    expect(FREE_ABOVE_PAISE, "free delivery starts above ₹999, which is 99900 paise").toBe(99900);
  });

  it("refuses a negative subtotal", () => {
    expect(() => deliveryFee(-6000), "a negative subtotal should throw a RangeError").toThrow(RangeError);
  });

  it("refuses a subtotal that is not whole paise", () => {
    expect(() => deliveryFee(450.5), "450.5 is not a whole number of paise, so it should throw a RangeError").toThrow(
      RangeError,
    );
  });
});
