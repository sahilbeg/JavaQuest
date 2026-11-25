import { MAX_QTY, type CartLine } from "./types.ts";

/**
 * Round 1. A customer logs in: combine the cart saved on the server with the one in this browser.
 * One line per item, saved lines first, the browser's newer name and price, never above MAX_QTY,
 * no empty lines, and neither cart is changed.
 */
export function mergeCarts(saved: CartLine[], current: CartLine[]): CartLine[] {
  // TODO 1: the candidate's answer. It changes the saved cart, goes above MAX_QTY, keeps the old price and keeps empty lines.
  // Rewrite it so it builds new lines and leaves both carts alone. Typing the parameters readonly CartLine[] lets tsc help.
  const merged = saved;
  for (const line of current) {
    const match = merged.find((m) => m.id === line.id);
    if (match) {
      match.qty += line.qty;
    } else {
      merged.push(line);
    }
  }
  return merged;
}
