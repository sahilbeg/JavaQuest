// Checkout's UPI step, written with promises.

/**
 * Returns a promise that is fulfilled after ms milliseconds.
 * Checkout uses it to wait a moment before asking the bank again.
 */
export function wait(ms) {
  // TODO 1: return a new Promise that calls resolve after ms milliseconds, using setTimeout.
  return Promise.resolve();
}

/**
 * payment is a promise from the bank. It is fulfilled with { status, ref }, or rejected with an Error.
 * Returns a promise of the text to show:
 *   status "SUCCESS"      -> "Payment received, ref UPI-5521" (with the real ref)
 *   any other status      -> "Payment pending"
 *   the payment failed    -> "Payment failed. Please try again."
 */
export function paymentMessage(payment) {
  // TODO 2: chain .then on payment and return the right text for the status.
  // TODO 3: add .catch that returns "Payment failed. Please try again."
  return Promise.resolve("");
}

/**
 * Calls closeQr() once payment settles, whether it worked or failed.
 * Returns a promise with the same outcome as payment: the same value, or the same error.
 */
export function closeQrWhenSettled(payment, closeQr) {
  // TODO 4: use .finally so closeQr runs either way, and return that promise.
  return payment;
}
