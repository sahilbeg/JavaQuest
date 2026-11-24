// The Diwali offer: 10% off orders above ₹1,500, with the discount rounded down to whole paise.

export const DIWALI_MIN_PAISE = 150000;

export function applyDiwaliOffer(cart) {
  const before = cart.lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const discountPaise = before > DIWALI_MIN_PAISE ? Math.floor(before / 10) : 0;
  return { ...cart, discountPaise, totalPaise: before - discountPaise };
}
