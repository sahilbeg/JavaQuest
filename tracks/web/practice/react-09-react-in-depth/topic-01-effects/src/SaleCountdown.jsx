import { useEffect, useState } from "react";

/** The Diwali sale banner: counts down from `seconds` to zero, one second at a time. */
export function SaleCountdown({ seconds }) {
  const [left, setLeft] = useState(seconds);

  // TODO 2: after the first render, start a timer that takes one second off left every 1000 ms, stopping at 0.
  // TODO 3: return a cleanup that stops the timer when the banner leaves the page.

  if (left === 0) return <p>The Diwali sale is over.</p>;
  return <p>Sale ends in {left} s</p>;
}
