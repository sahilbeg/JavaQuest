import { describe, it, expect } from "vitest";
import { checkoutReducer, extrasPaise, stepTitle, START } from "../src/checkout.ts";

describe("Checkout (gold)", () => {
  it("gives every step its own title", () => {
    const titles = ["cart", "address", "gift", "upi", "done"].map((step) => stepTitle(step));
    expect(titles, "each step should have its own heading").toEqual([
      "Your cart",
      "Delivery address",
      "Make it a gift",
      "Pay by UPI",
      "Order placed",
    ]);
  });

  it("goes nowhere past the ends", () => {
    const done = { ...START, step: "done" };
    expect(checkoutReducer(done, { type: "next" }).step, "Continue on done should stay on done").toBe("done");
    expect(checkoutReducer(done, { type: "back" }).step, "Back on done should stay on done: the order is placed").toBe("done");
    expect(checkoutReducer(START, { type: "back" }).step, "Back on the cart should stay on the cart").toBe("cart");
  });

  it("throws on an action nobody handles", () => {
    expect(() => checkoutReducer(START, { type: "skipToPayment" }), "an unknown action should throw Unknown action: skipToPayment").toThrow(
      "Unknown action: skipToPayment",
    );
  });

  it("charges for a stitched name only when there is one", () => {
    expect(extrasPaise({ ...START, stitchedName: "   " }), "a name of only spaces should cost nothing").toBe(0);
    expect(extrasPaise({ ...START, stitchedName: "Rohan" }), "a real name costs 5000 paise").toBe(5000);
    expect(extrasPaise({ ...START, giftWrap: true, stitchedName: " Rohan " }), "gift wrap and a name cost 8000 paise").toBe(8000);
  });
});
