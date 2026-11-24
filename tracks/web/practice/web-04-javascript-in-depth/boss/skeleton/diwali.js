// The Diwali offer: 10% off orders above ₹1,500, with the discount rounded down to whole paise.

export const DIWALI_MIN_PAISE = 150000;

export function applyDiwaliOffer(cart) {
  // TODO 1 (bronze): the offer must apply once per order, not once per line.
  // TODO 2 (silver): this writes into the cart it was given. Return a new cart object instead.
  // TODO 3 (gold): a second call must give the same answer. Work from the lines, not from totalPaise.
  for (const line of cart.lines) {
    if (cart.totalPaise > DIWALI_MIN_PAISE) {
      const off = Math.floor(cart.totalPaise / 10);
      cart.totalPaise = cart.totalPaise - off;
      cart.discountPaise = off;
    }
  }
  return cart;
}
