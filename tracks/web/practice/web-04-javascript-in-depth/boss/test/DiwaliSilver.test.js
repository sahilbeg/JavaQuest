import { describe, it, expect } from "vitest";
import { applyDiwaliOffer } from "../src/diwali.js";
import { raoCart, smallCart } from "./carts.js";

describe("Silver: the cart passed in is never changed", () => {
  it("leaves Mrs. Rao's cart exactly as it was", () => {
    const cart = raoCart();
    applyDiwaliOffer(cart);
    expect(cart.totalPaise, "the cart passed in must still say ₹1,948.00").toBe(194800);
    expect("discountPaise" in cart, "the cart passed in must not get a discountPaise field").toBe(false);
  });

  it("leaves a small cart alone too", () => {
    const cart = smallCart();
    applyDiwaliOffer(cart);
    expect(cart, "a cart with no offer must come out unchanged as well").toEqual(smallCart());
    expect("discountPaise" in cart, "not even discountPaise 0 should be written into it").toBe(false);
  });

  it("returns a new cart object", () => {
    const cart = raoCart();
    const result = applyDiwaliOffer(cart);
    expect(result === cart, "applyDiwaliOffer should return a new object, not the one it was given").toBe(false);
  });

  it("keeps the id and the lines", () => {
    const result = applyDiwaliOffer(raoCart());
    expect(result.id, "the new cart should keep the order id").toBe("order-17");
    expect(result.lines, "the new cart should keep the same lines").toEqual(raoCart().lines);
  });

  it("gives the same answer for the same cart, call after call", () => {
    const cart = raoCart();
    const first = applyDiwaliOffer(cart);
    const second = applyDiwaliOffer(cart);
    expect(second.totalPaise, "opening the cart a second time must not lower the price again").toBe(first.totalPaise);
    expect(second.totalPaise, "both calls should give ₹1,753.20").toBe(175320);
  });
});
