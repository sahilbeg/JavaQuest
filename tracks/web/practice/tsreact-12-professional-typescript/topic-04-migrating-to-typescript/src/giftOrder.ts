import { formatRupees } from "./money.ts";

/** Moved from giftOrder.js. Gift wrap costs 3000 paise and a stitched name 5000 paise, for each item. */

// Step 1 of the move is done: the file is renamed, and every shape is typed any, so it compiles exactly as the JavaScript did.
// TODO 1: replace the three any types with real object types (see the README). The editor will then show the bugs below.
export type GiftOptions = any;
export type GiftLine = any;
export type GiftOrder = any;

/** The extras for one item, in paise. */
export function extrasPaise(gift: GiftOptions): number {
  // TODO 2: fix what the types show here. A stitched name of only spaces is no name, and costs nothing.
  let paise = 0;
  if (gift.giftwrap) paise += 3000;
  if (gift.stitchedName) paise += 5000;
  return paise;
}

/** One line: the item and its extras, times the quantity. */
export function lineTotal(line: GiftLine): number {
  // TODO 3: fix what the types show here. The extras are paid for every item in the line, not once.
  return line.paise * line.quantity + extrasPaise(line.gift);
}

/** "3 items, ₹1,180.00": how many items, and what the order costs. */
export function orderSummary(order: GiftOrder): string {
  // TODO 4: an order has no total field. Count every item (the sum of qty), add up lineTotal for every line, and say "1 item" for one.
  const count = order.lines.length;
  return count + " items, " + formatRupees(order.total);
}
