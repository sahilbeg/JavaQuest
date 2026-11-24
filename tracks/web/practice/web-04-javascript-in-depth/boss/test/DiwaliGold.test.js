import { describe, it, expect } from "vitest";
import { applyDiwaliOffer } from "../src/diwali.js";
import { raoCart, nearLimitCart, smallCart } from "./carts.js";

describe("Gold: applying the offer again changes nothing", () => {
  it("gives the same cart when run on its own result", () => {
    const once = applyDiwaliOffer(raoCart());
    const twice = applyDiwaliOffer(once);
    expect(twice.discountPaise, "the discount should still be ₹194.80, not a second 10%").toBe(19480);
    expect(twice.totalPaise, "the total should still be ₹1,753.20").toBe(175320);
  });

  it("stays the same after three runs", () => {
    const thrice = applyDiwaliOffer(applyDiwaliOffer(applyDiwaliOffer(raoCart())));
    expect(thrice, "three runs should equal one run").toEqual(applyDiwaliOffer(raoCart()));
  });

  it("keeps the discount when the discounted total is below ₹1,500", () => {
    const once = applyDiwaliOffer(nearLimitCart());
    expect(once.discountPaise, "₹1,589.00 is above ₹1,500, so ₹158.90 comes off").toBe(15890);
    const twice = applyDiwaliOffer(once);
    expect(twice.discountPaise, "the cart was ₹1,589.00 before the discount, so it must keep its ₹158.90 off").toBe(15890);
    expect(twice.totalPaise, "the customer should still pay ₹1,430.10").toBe(143010);
  });

  it("keeps a cart without the offer at its full price", () => {
    const twice = applyDiwaliOffer(applyDiwaliOffer(smallCart()));
    expect(twice.discountPaise, "a ₹1,250.00 cart never gets a discount").toBe(0);
    expect(twice.totalPaise, "it should stay ₹1,250.00").toBe(125000);
  });
});
