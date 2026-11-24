// The "Added to cart" toast and the UPI QR countdown.

// The id of the toast's hide timer. It lives out here, so every call to showToast shares it.
let hideTimer = null;

/**
 * Shows text in the toast element el, then hides it after ms milliseconds.
 * A new toast cancels the old hide timer, so the new message stays up for its full time.
 */
export function showToast(el, text, ms = 3000) {
  el.textContent = text;
  el.hidden = false;
  clearTimeout(hideTimer);
  hideTimer = setTimeout(() => {
    el.hidden = true;
  }, ms);
}

/**
 * Counts down from seconds, one step every 1000 ms.
 * While some seconds are left, it calls onTick(secondsLeft). At 0 it stops and calls onDone() once.
 * Returns a function that stops the countdown early.
 */
export function startCountdown(seconds, onTick, onDone) {
  let left = seconds;
  const id = setInterval(() => {
    left = left - 1;
    if (left > 0) {
      onTick(left);
    } else {
      clearInterval(id);
      onDone();
    }
  }, 1000);
  return () => clearInterval(id);
}
