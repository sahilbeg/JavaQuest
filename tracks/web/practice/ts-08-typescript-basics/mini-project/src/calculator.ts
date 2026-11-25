// The typed price calculator. Every amount is whole paise: ₹1 is 100 paise.
import { ITEM_CODES, PRICES, GIFT_WRAP, NAME_STITCHED, DELIVERY_FEE, FREE_DELIVERY_ABOVE, DIWALI_ABOVE } from "./prices.ts";
import type { ItemCode } from "./prices.ts";
import type { CartLine, Delivery, Order, Quote } from "./types.ts";
import { formatRupees } from "./money.ts";

export function parseCode(text: string): ItemCode | null {
  // TODO 1: trim and upper-case the text; return the matching code from ITEM_CODES, or null
  return null;
}

export function itemsTotal(lines: CartLine[]): number {
  // TODO 2: add up the item's price times its quantity, for every line
  return 0;
}

export function extrasTotal(lines: CartLine[]): number {
  // TODO 3: per item, GIFT_WRAP when giftWrap is true and NAME_STITCHED when there is a name; times the quantity
  return 0;
}

export function diwaliDiscount(subtotal: number): number {
  // TODO 4: 10% of the subtotal, rounded down, only when the subtotal is above DIWALI_ABOVE; otherwise 0
  return 0;
}

export function deliveryFee(subtotal: number, delivery: Delivery): number {
  // TODO 5: 0 for a pickup. In Pune, 0 when the subtotal is above FREE_DELIVERY_ABOVE, and DELIVERY_FEE otherwise.
  return 0;
}

export function quote(order: Order): Quote {
  // TODO 6: items and extras, subtotal = items + extras, the discount and delivery from the subtotal,
  //         and total = subtotal - discount + delivery
  return { items: 0, extras: 0, subtotal: 0, discount: 0, delivery: 0, total: 0 };
}

export function receipt(order: Order): string {
  // TODO 7: the quote as text, one row per line, joined with "\n" (see README.md for the exact rows)
  return "";
}
