# Timers and callbacks

Fix the cart's toast and build the QR countdown. Change `src/timers.js`.

**The toast: `showToast(el, text, ms = 3000)`**

1. **TODO 1:** put `text` into `el.textContent` and show the element with `el.hidden = false`.
2. **TODO 2:** cancel the old hide timer with `clearTimeout(hideTimer)`. `hideTimer` is already declared at the top of the file.
3. **TODO 3:** start a new `setTimeout` that sets `el.hidden = true` after `ms` milliseconds, and keep its id in `hideTimer`.

So when a second toast arrives early, it gets its full 3 seconds.

**The countdown: `startCountdown(seconds, onTick, onDone)`**

4. **TODO 4:** start a `setInterval` that runs every 1000 ms and takes one off the seconds left.
5. **TODO 5:** while the seconds left are above 0, call `onTick(left)`. When they reach 0, stop the interval with `clearInterval` and call `onDone()` once. Don't call `onTick(0)`.
6. **TODO 6:** return a function that stops the countdown early.

For example, `startCountdown(3, onTick, onDone)` calls `onTick(2)` after 1 second, `onTick(1)` after 2 seconds, and `onDone()` after 3 seconds.

Press **Submit** in Cadence, or save the file in your editor. The checks use pretend timers, so they finish at once.
