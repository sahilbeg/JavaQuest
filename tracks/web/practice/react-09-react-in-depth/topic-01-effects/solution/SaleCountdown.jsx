import { useEffect, useState } from "react";

/** The Diwali sale banner: counts down from `seconds` to zero, one second at a time. */
export function SaleCountdown({ seconds }) {
  const [left, setLeft] = useState(seconds);

  useEffect(() => {
    const id = setInterval(() => {
      setLeft((l) => Math.max(l - 1, 0));
    }, 1000);
    return () => clearInterval(id);
  }, []);

  if (left === 0) return <p>The Diwali sale is over.</p>;
  return <p>Sale ends in {left} s</p>;
}
