import { describe, it, expect } from "vitest";
import { checkoutTotals } from "../src/totals.js";
import { validateAddress } from "../src/address.js";
import { checkoutReducer, startCheckout } from "../src/checkoutReducer.js";

const ASHA = { name: "Asha Rao", phone: "9876543210", pincode: "411007" };

describe("checkoutTotals", () => {
  it("adds ₹60 delivery to a small order", () => {
    const lines = [
      { code: "CST", name: "Coaster set", paise: 15000, qty: 2 },
      { code: "AMI", name: "Amigurumi elephant", paise: 45000, qty: 1 },
    ];
    expect(checkoutTotals(lines), "2 coaster sets and an amigurumi are 75000 paise, plus 6000 delivery").toEqual({
      items: 75000,
      delivery: 6000,
      total: 81000,
    });
  });

  it("delivers free above ₹999", () => {
    const lines = [
      { code: "BAG", name: "Tote bag", paise: 89900, qty: 1 },
      { code: "CST", name: "Coaster set", paise: 15000, qty: 1 },
    ];
    expect(checkoutTotals(lines), "104900 paise is above ₹999, so delivery is free").toEqual({ items: 104900, delivery: 0, total: 104900 });
  });

  it("still charges delivery at exactly ₹999", () => {
    const lines = [{ code: "CUS", name: "Custom amigurumi", paise: 99900, qty: 1 }];
    expect(checkoutTotals(lines).delivery, "free delivery is only for orders above ₹999, not equal to it").toBe(6000);
  });

  it("charges nothing for an empty cart", () => {
    expect(checkoutTotals([]), "an empty cart costs nothing, with no delivery").toEqual({ items: 0, delivery: 0, total: 0 });
  });
});

describe("validateAddress", () => {
  it("accepts a good address", () => {
    expect(validateAddress({ name: "Asha Rao", phone: "98765 43210", pincode: "411007" }), "a good address has no errors").toEqual({});
  });

  it("finds every problem in an empty address, with the ticket 7 messages", () => {
    expect(validateAddress({ name: " ", phone: "98765", pincode: "01100" }), "all three fields are wrong here").toEqual({
      name: "Please enter your name.",
      phone: "Phone needs 10 digits, like 98765 43210.",
      pincode: "PIN code needs 6 digits.",
    });
  });
});

describe("checkoutReducer", () => {
  it("starts on the cart step", () => {
    expect(startCheckout, "checkout should start as { step: \"cart\", address: null, orderId: null }").toEqual({
      step: "cart",
      address: null,
      orderId: null,
    });
  });

  it("walks through every step in order", () => {
    let state = checkoutReducer(startCheckout, { type: "cartConfirmed" });
    expect(state.step, "cartConfirmed should move from cart to address").toBe("address");
    state = checkoutReducer(state, { type: "addressSaved", address: ASHA });
    expect(state.step, "addressSaved should move from address to upi").toBe("upi");
    expect(state.address, "addressSaved should keep the address").toEqual(ASHA);
    state = checkoutReducer(state, { type: "paid", orderId: "order-17" });
    expect(state.step, "paid should move from upi to done").toBe("done");
    expect(state.orderId, "paid should keep the order id").toBe("order-17");
  });

  it("goes back one step at a time and keeps the address", () => {
    const onUpi = { step: "upi", address: ASHA, orderId: null };
    const onAddress = checkoutReducer(onUpi, { type: "wentBack" });
    expect(onAddress, "wentBack from upi should go to address and keep the address").toEqual({ step: "address", address: ASHA, orderId: null });
    expect(checkoutReducer(onAddress, { type: "wentBack" }).step, "wentBack from address should go to cart").toBe("cart");
  });

  it("refuses to skip a step", () => {
    const paidTooSoon = checkoutReducer(startCheckout, { type: "paid", orderId: "order-17" });
    expect(paidTooSoon.step, "paid on the cart step must not jump to done").toBe("cart");
    const addressTooSoon = checkoutReducer(startCheckout, { type: "addressSaved", address: ASHA });
    expect(addressTooSoon.step, "addressSaved on the cart step must not jump to upi").toBe("cart");
  });

  it("never changes the state it was given", () => {
    const state = { step: "address", address: null, orderId: null };
    const before = JSON.stringify(state);
    const next = checkoutReducer(state, { type: "addressSaved", address: ASHA });
    expect(next, "addressSaved should return a new state object").not.toBe(state);
    expect(JSON.stringify(state), "the old state should be left exactly as it was").toBe(before);
  });

  it("throws for an action it doesn't know", () => {
    expect(() => checkoutReducer(startCheckout, { type: "couponApplied" }), "unknown types should throw Unknown action: couponApplied").toThrow(
      "Unknown action: couponApplied",
    );
  });
});
