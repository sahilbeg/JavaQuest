// Dhaaga's rule book, all in whole paise:
// - subtotal: every line's paise × qty, added up;
// - Diwali offer: 10% off a subtotal above 150000, rounded down;
// - delivery: 6000, free when the total after the offer is above 99900, and nothing for an empty cart;
// - total: subtotal − offer + delivery.
export function orderTotals(lines) {
  // TODO 1: work out { subtotal, offer, delivery, total } from lines like [{ paise: 45000, qty: 2 }].
  return { subtotal: 0, offer: 0, delivery: 0, total: 0 };
}
