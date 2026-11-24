export const DELIVERY_PAISE = 6000;
export const FREE_DELIVERY_ABOVE_PAISE = 99900;
export const OFFER_ABOVE_PAISE = 150000;

/**
 * The totals for a cart. lines is [{ id, name, paise, qty }].
 * Returns { subtotal, discount, delivery, total }, all in paise.
 */
export function cartTotals(lines) {
  // TODO 1: subtotal is paise × qty for every line. The Diwali offer takes 10% off a subtotal above ₹1,500, rounded down.
  // Delivery is ₹60, free when the total after the offer is above ₹999, and 0 for an empty cart. total = subtotal - discount + delivery.
  return { subtotal: 0, discount: 0, delivery: 0, total: 0 };
}
