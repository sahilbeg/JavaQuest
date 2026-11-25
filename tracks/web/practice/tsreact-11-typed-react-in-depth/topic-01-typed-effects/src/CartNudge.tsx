import { useEffect, useState } from "react";

export interface CartNudgeProps {
  count: number;
  delayMs: number;
}

/** After the cart has stayed the same for delayMs, a gentle reminder appears under it. */
export function CartNudge({ count, delayMs }: CartNudgeProps) {
  // The cart count the reminder was shown for. null means "not shown yet".
  const [nudgedAt, setNudgedAt] = useState<number | null>(null);

  // TODO 3: when count is above 0, start a setTimeout that calls setNudgedAt(count) after delayMs.
  //         Keep its id in a const typed ReturnType<typeof setTimeout>.
  // TODO 4: return a cleanup that clears the timer. List [count, delayMs] as the dependencies.

  if (count === 0 || nudgedAt !== count) return null;
  return (
    <p role="status">
      Still thinking? Your {count} {count === 1 ? "item is" : "items are"} saved in the cart.
    </p>
  );
}
