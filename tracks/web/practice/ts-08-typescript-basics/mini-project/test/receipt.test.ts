import { describe, it, expect } from "vitest";
import { receipt } from "../src/calculator.ts";

describe("The quote text for WhatsApp", () => {
  it("writes Mrs. Joshi's quote with the Diwali offer and free delivery", () => {
    const text = receipt({
      lines: [
        { code: "BAG", qty: 2 },
        { code: "BTY", qty: 1, giftWrap: true },
      ],
      delivery: "pune",
      payment: "UPI",
    });
    expect(text, "the quote should match the design in the brief, row for row").toBe(
      [
        "Dhaaga & Co. price quote",
        "BAG x 2: ₹1,798.00",
        "BTY x 1: ₹350.00",
        "Extras: ₹30.00",
        "Subtotal: ₹2,178.00",
        "Diwali offer: -₹217.80",
        "Delivery: FREE",
        "Total: ₹1,960.20",
        "Payment: UPI",
      ].join("\n"),
    );
  });

  it("leaves out the offer row and shows the delivery fee on a small order", () => {
    const text = receipt({
      lines: [
        { code: "CST", qty: 1 },
        { code: "KEY", qty: 2, name: "Riya" },
      ],
      delivery: "pune",
      payment: "CASH",
    });
    expect(text, "no Diwali offer row, and Delivery: ₹60.00").toBe(
      [
        "Dhaaga & Co. price quote",
        "CST x 1: ₹150.00",
        "KEY x 2: ₹240.00",
        "Extras: ₹100.00",
        "Subtotal: ₹490.00",
        "Delivery: ₹60.00",
        "Total: ₹550.00",
        "Payment: CASH",
      ].join("\n"),
    );
  });

  it("says Pickup at the stall for a pickup order", () => {
    const text = receipt({ lines: [{ code: "AMI", qty: 1 }], delivery: "pickup", payment: "CARD" });
    expect(text, "a pickup has no delivery fee, and its own delivery row").toBe(
      [
        "Dhaaga & Co. price quote",
        "AMI x 1: ₹450.00",
        "Extras: ₹0.00",
        "Subtotal: ₹450.00",
        "Delivery: Pickup at the stall",
        "Total: ₹450.00",
        "Payment: CARD",
      ].join("\n"),
    );
    expect(text.endsWith("\n"), "there should be no newline after the last row").toBe(false);
  });
});
