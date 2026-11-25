import type { CartLine, Totals } from "./types.ts";

/** The Diwali offer starts above ₹1,500. */
export const OFFER_ABOVE = 150000;
/** Delivery inside Pune is free above ₹999, counted after the offer. */
export const FREE_DELIVERY_ABOVE = 99900;
/** Delivery costs ₹60. */
export const DELIVERY_PAISE = 6000;

/** The subtotal, the Diwali offer, delivery and the total, all in paise. */
export function cartTotals(lines: readonly CartLine[]): Totals {
  const subtotal = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const discount = subtotal > OFFER_ABOVE ? Math.floor(subtotal / 10) : 0;
  const afterOffer = subtotal - discount;
  const delivery = lines.length === 0 || afterOffer > FREE_DELIVERY_ABOVE ? 0 : DELIVERY_PAISE;
  return { subtotal, discount, delivery, total: afterOffer + delivery };
}
