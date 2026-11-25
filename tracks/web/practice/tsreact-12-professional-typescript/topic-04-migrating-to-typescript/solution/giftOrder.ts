import { formatRupees } from "./money.ts";

/** Moved from giftOrder.js. Gift wrap costs 3000 paise and a stitched name 5000 paise, for each item. */

export interface GiftOptions {
  giftWrap: boolean;
  stitchedName: string;
}

export interface GiftLine {
  id: string;
  name: string;
  paise: number;
  qty: number;
  gift: GiftOptions;
}

export interface GiftOrder {
  id: string;
  lines: GiftLine[];
}

/** The extras for one item, in paise. */
export function extrasPaise(gift: GiftOptions): number {
  let paise = 0;
  if (gift.giftWrap) paise += 3000;
  if (gift.stitchedName.trim() !== "") paise += 5000;
  return paise;
}

/** One line: the item and its extras, times the quantity. */
export function lineTotal(line: GiftLine): number {
  return (line.paise + extrasPaise(line.gift)) * line.qty;
}

/** "3 items, ₹1,180.00": how many items, and what the order costs. */
export function orderSummary(order: GiftOrder): string {
  const count = order.lines.reduce((sum, line) => sum + line.qty, 0);
  const total = order.lines.reduce((sum, line) => sum + lineTotal(line), 0);
  return `${count} ${count === 1 ? "item" : "items"}, ${formatRupees(total)}`;
}
