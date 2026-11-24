import { describe, it, expect } from "vitest";
import { applyDiwaliOffer } from "../src/diwali.js";
import { raoCart, threeLineCart, twoBagsCart, smallCart, exactlyLimitCart } from "./carts.js";

describe("Bronze: the offer is applied once per order", () => {
  it("takes 10% off Mrs. Rao's two-line cart once", () => {
    const result = applyDiwaliOffer(raoCart());
    expect(result.discountPaise, "10% of ₹1,948.00 is ₹194.80, taken once").toBe(19480);
    expect(result.totalPaise, "Mrs. Rao should pay ₹1,753.20, not ₹1,577.88").toBe(175320);
  });

  it("takes 10% off a three-line cart once", () => {
    const result = applyDiwaliOffer(threeLineCart());
    expect(result.discountPaise, "10% of ₹1,919.00 is ₹191.90, taken once").toBe(19190);
    expect(result.totalPaise, "the customer should pay ₹1,727.10").toBe(172710);
  });

  it("takes 10% off a one-line cart above ₹1,500", () => {
    const result = applyDiwaliOffer(twoBagsCart());
    expect(result.discountPaise, "10% of ₹1,798.00 is ₹179.80").toBe(17980);
    expect(result.totalPaise, "two tote bags should cost ₹1,618.20").toBe(161820);
  });

  it("gives no discount below ₹1,500", () => {
    const result = applyDiwaliOffer(smallCart());
    expect(result.discountPaise, "a ₹1,250.00 cart gets no Diwali discount, so discountPaise is 0").toBe(0);
    expect(result.totalPaise, "the total should stay ₹1,250.00").toBe(125000);
  });

  it("gives no discount at exactly ₹1,500", () => {
    const result = applyDiwaliOffer(exactlyLimitCart());
    expect(result.discountPaise, "the offer is for orders above ₹1,500, so ₹1,500.00 gets nothing").toBe(0);
    expect(result.totalPaise, "the total should stay ₹1,500.00").toBe(150000);
  });
});
