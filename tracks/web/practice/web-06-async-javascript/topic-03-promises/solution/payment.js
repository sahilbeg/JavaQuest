// Checkout's UPI step, written with promises.

/**
 * Returns a promise that is fulfilled after ms milliseconds.
 * Checkout uses it to wait a moment before asking the bank again.
 */
export function wait(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

/**
 * payment is a promise from the bank. It is fulfilled with { status, ref }, or rejected with an Error.
 * Returns a promise of the text to show:
 *   status "SUCCESS"      -> "Payment received, ref UPI-5521" (with the real ref)
 *   any other status      -> "Payment pending"
 *   the payment failed    -> "Payment failed. Please try again."
 */
export function paymentMessage(payment) {
  return payment
    .then((result) => (result.status === "SUCCESS" ? `Payment received, ref ${result.ref}` : "Payment pending"))
    .catch(() => "Payment failed. Please try again.");
}

/**
 * Calls closeQr() once payment settles, whether it worked or failed.
 * Returns a promise with the same outcome as payment: the same value, or the same error.
 */
export function closeQrWhenSettled(payment, closeQr) {
  return payment.finally(closeQr);
}
