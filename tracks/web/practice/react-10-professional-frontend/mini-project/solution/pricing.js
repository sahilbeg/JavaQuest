export const DELIVERY_PAISE = 6000;
export const FREE_DELIVERY_ABOVE_PAISE = 99900;
export const OFFER_ABOVE_PAISE = 150000;

/**
 * The totals for a cart. lines is [{ id, name, paise, qty }].
 * Returns { subtotal, discount, delivery, total }, all in paise.
 */
export function cartTotals(lines) {
  const subtotal = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const discount = subtotal > OFFER_ABOVE_PAISE ? Math.floor(subtotal / 10) : 0;
  const afterOffer = subtotal - discount;
  const delivery = subtotal === 0 || afterOffer > FREE_DELIVERY_ABOVE_PAISE ? 0 : DELIVERY_PAISE;
  return { subtotal, discount, delivery, total: afterOffer + delivery };
}
