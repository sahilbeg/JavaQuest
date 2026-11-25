// Dhaaga's price rules, in paise.
export const DELIVERY_PAISE = 6000;
export const FREE_DELIVERY_ABOVE = 99900;
export const OFFER_ABOVE = 150000;

/** Works out { subtotal, discount, delivery, total } in paise for lines like { id, name, paise, qty }. */
export function cartTotals(lines) {
  // TODO 1: subtotal = every line's paise * qty, added up.
  //         discount = 10% of the subtotal, rounded down, only when the subtotal is above OFFER_ABOVE; else 0.
  //         delivery = DELIVERY_PAISE, but 0 for an empty cart or when subtotal - discount is above FREE_DELIVERY_ABOVE.
  //         total = subtotal - discount + delivery.
  return { subtotal: 0, discount: 0, delivery: 0, total: 0 };
}
