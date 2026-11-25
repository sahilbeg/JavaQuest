import { useEffect, useState } from "react";

export interface CartNudgeProps {
  count: number;
  delayMs: number;
}

/** After the cart has stayed the same for delayMs, a gentle reminder appears under it. */
export function CartNudge({ count, delayMs }: CartNudgeProps) {
  // The cart count the reminder was shown for. null means "not shown yet".
  const [nudgedAt, setNudgedAt] = useState<number | null>(null);

  useEffect(() => {
    if (count === 0) return;
    const id: ReturnType<typeof setTimeout> = setTimeout(() => setNudgedAt(count), delayMs);
    return () => clearTimeout(id);
  }, [count, delayMs]);

  if (count === 0 || nudgedAt !== count) return null;
  return (
    <p role="status">
      Still thinking? Your {count} {count === 1 ? "item is" : "items are"} saved in the cart.
    </p>
  );
}
