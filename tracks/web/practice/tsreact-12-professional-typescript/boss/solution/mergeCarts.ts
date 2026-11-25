import { MAX_QTY, type CartLine } from "./types.ts";

/**
 * Round 1. A customer logs in: combine the cart saved on the server with the one in this browser.
 * One line per item, saved lines first, the browser's newer name and price, never above MAX_QTY,
 * no empty lines, and neither cart is changed.
 */
export function mergeCarts(saved: readonly CartLine[], current: readonly CartLine[]): CartLine[] {
  const merged = new Map<string, CartLine>();
  for (const line of [...saved, ...current]) {
    if (line.qty <= 0) continue;
    const before = merged.get(line.id);
    merged.set(line.id, { ...line, qty: Math.min(MAX_QTY, (before?.qty ?? 0) + line.qty) });
  }
  return [...merged.values()];
}
