import { describe, it, expect, vi } from "vitest";
import { closeQrWhenSettled } from "../src/payment.js";

/** A payment promise that the bank rejected. The empty catch stops it counting as "unhandled" before your code sees it. */
function failedPayment(message) {
  const payment = Promise.reject(new Error(message));
  payment.catch(() => {});
  return payment;
}

describe("closeQrWhenSettled", () => {
  it("closes the QR after a successful payment and passes the result on", async () => {
    const closeQr = vi.fn();
    const result = await closeQrWhenSettled(Promise.resolve({ status: "SUCCESS", ref: "UPI-5521" }), closeQr);
    expect(closeQr.mock.calls.length, "closeQr should be called once after a successful payment").toBe(1);
    expect(result, "the payment's result should pass through unchanged").toEqual({ status: "SUCCESS", ref: "UPI-5521" });
  });

  it("closes the QR after a failed payment and keeps the failure", async () => {
    const closeQr = vi.fn();
    let error = null;
    try {
      await closeQrWhenSettled(failedPayment("Bank app closed"), closeQr);
    } catch (e) {
      error = e;
    }
    expect(closeQr.mock.calls.length, "closeQr should be called once after a failed payment too").toBe(1);
    expect(error?.message, "the returned promise should still reject with the bank's error").toBe("Bank app closed");
  });

  it("waits for the payment before closing the QR", async () => {
    const closeQr = vi.fn();
    let bankAnswers;
    const payment = new Promise((resolve) => {
      bankAnswers = resolve;
    });
    const done = closeQrWhenSettled(payment, closeQr);
    await Promise.resolve();
    expect(closeQr.mock.calls.length, "the QR must stay open while the payment is still pending").toBe(0);
    bankAnswers({ status: "SUCCESS", ref: "UPI-5521" });
    await done;
    expect(closeQr.mock.calls.length, "the QR should close once the payment settles").toBe(1);
  });
});
