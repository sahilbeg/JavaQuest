// The "Added to cart" toast and the UPI QR countdown.

// The id of the toast's hide timer. It lives out here, so every call to showToast shares it.
let hideTimer = null;

/**
 * Shows text in the toast element el, then hides it after ms milliseconds.
 * A new toast cancels the old hide timer, so the new message stays up for its full time.
 */
export function showToast(el, text, ms = 3000) {
  // TODO 1: put text into el.textContent and make el visible with el.hidden = false.
  // TODO 2: cancel the old hide timer with clearTimeout(hideTimer).
  // TODO 3: start a new timer with setTimeout that sets el.hidden = true after ms. Keep its id in hideTimer.
}

/**
 * Counts down from seconds, one step every 1000 ms.
 * While some seconds are left, it calls onTick(secondsLeft). At 0 it stops and calls onDone() once.
 * Returns a function that stops the countdown early.
 */
export function startCountdown(seconds, onTick, onDone) {
  // TODO 4: start a setInterval that runs every 1000 ms and takes one off the seconds left.
  // TODO 5: while the seconds left are above 0, call onTick(left). At 0, call clearInterval and then onDone().
  // TODO 6: return a function that stops the interval with clearInterval.
  return () => {};
}
