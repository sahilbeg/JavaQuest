import { describe, it, expect } from "vitest";
import { checkoutReducer, START } from "../src/checkoutMachine.ts";

const ASHA = { name: "Asha Rao", phone: "9876543210", pincode: "411007" };

describe("checkoutReducer", () => {
  it("moves from the cart to the address step", () => {
    expect(checkoutReducer(START, { type: "cartConfirmed" }), "cartConfirmed on the cart gives an empty address step").toEqual({
      step: "address",
      saved: null,
    });
  });

  it("moves to UPI with the saved address", () => {
    const state = checkoutReducer({ step: "address", saved: null }, { type: "addressSaved", address: ASHA });
    expect(state, "addressSaved gives the upi step, carrying the address").toEqual({ step: "upi", address: ASHA });
  });

  it("goes back one step at a time, keeping the address", () => {
    const back = checkoutReducer({ step: "upi", address: ASHA }, { type: "wentBack" });
    expect(back, "Back from upi gives the address step with Asha's address saved").toEqual({ step: "address", saved: ASHA });
    expect(checkoutReducer(back, { type: "wentBack" }), "Back from the address gives the cart").toEqual({ step: "cart" });
  });

  it("finishes with the order id", () => {
    const state = checkoutReducer({ step: "upi", address: ASHA }, { type: "paid", orderId: "order-17" });
    expect(state, "paid on upi gives done, with the address and order-17").toEqual({ step: "done", address: ASHA, orderId: "order-17" });
  });

  it("ignores an action on the wrong step", () => {
    const cart = { step: "cart" };
    expect(checkoutReducer(cart, { type: "paid", orderId: "order-17" }), "paying on the cart step must change nothing").toBe(cart);
    const done = { step: "done", address: ASHA, orderId: "order-17" };
    expect(checkoutReducer(done, { type: "wentBack" }), "going back after the order is placed must change nothing").toBe(done);
    const address = { step: "address", saved: null };
    expect(checkoutReducer(address, { type: "cartConfirmed" }), "cartConfirmed on the address step must change nothing").toBe(address);
  });

  it("throws on an action it doesn't know", () => {
    expect(() => checkoutReducer(START, { type: "skipToPayment" }), "an unknown action should throw Unknown action: skipToPayment").toThrow(
      "Unknown action: skipToPayment",
    );
  });
});
