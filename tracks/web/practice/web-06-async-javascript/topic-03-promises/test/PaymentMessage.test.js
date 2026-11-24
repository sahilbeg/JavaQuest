import { describe, it, expect } from "vitest";
import { paymentMessage } from "../src/payment.js";

/** A payment promise that the bank rejected. The empty catch stops it counting as "unhandled" before your code sees it. */
function failedPayment(message) {
  const payment = Promise.reject(new Error(message));
  payment.catch(() => {});
  return payment;
}

describe("paymentMessage", () => {
  it("thanks the customer and shows the reference when the payment succeeds", async () => {
    const text = await paymentMessage(Promise.resolve({ status: "SUCCESS", ref: "UPI-5521" }));
    expect(text, "a SUCCESS payment should give the text with its ref").toBe("Payment received, ref UPI-5521");
  });

  it("uses the reference from this payment, not a fixed one", async () => {
    const text = await paymentMessage(Promise.resolve({ status: "SUCCESS", ref: "UPI-7730" }));
    expect(text, "the message should include this payment's ref, UPI-7730").toBe("Payment received, ref UPI-7730");
  });

  it("says the payment is pending for any other status", async () => {
    const text = await paymentMessage(Promise.resolve({ status: "PENDING", ref: "UPI-8810" }));
    expect(text, "a status other than SUCCESS should give \"Payment pending\"").toBe("Payment pending");
  });

  it("turns a failed payment into a friendly message instead of an error", async () => {
    let text = null;
    let error = null;
    try {
      text = await paymentMessage(failedPayment("Bank app closed"));
    } catch (e) {
      error = e;
    }
    expect(error, "paymentMessage should catch the failure itself, so its promise never rejects").toBeNull();
    expect(text, "a failed payment should give the retry message").toBe("Payment failed. Please try again.");
  });
});
