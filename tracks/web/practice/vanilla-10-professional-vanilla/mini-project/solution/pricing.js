// Dhaaga's price rules, in paise.
export const DELIVERY_PAISE = 6000;
export const FREE_DELIVERY_ABOVE = 99900;
export const OFFER_ABOVE = 150000;

/** Works out { subtotal, discount, delivery, total } in paise for lines like { id, name, paise, qty }. */
export function cartTotals(lines) {
  const subtotal = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const discount = subtotal > OFFER_ABOVE ? Math.floor(subtotal / 10) : 0;
  const afterOffer = subtotal - discount;
  const delivery = lines.length === 0 || afterOffer > FREE_DELIVERY_ABOVE ? 0 : DELIVERY_PAISE;
  return { subtotal, discount, delivery, total: afterOffer + delivery };
}
