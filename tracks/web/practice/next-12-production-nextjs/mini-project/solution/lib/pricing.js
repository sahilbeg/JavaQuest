// Dhaaga's rule book, all in whole paise:
// - subtotal: every line's paise × qty, added up;
// - Diwali offer: 10% off a subtotal above 150000, rounded down;
// - delivery: 6000, free when the total after the offer is above 99900, and nothing for an empty cart;
// - total: subtotal − offer + delivery.
export function orderTotals(lines) {
  const subtotal = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const offer = subtotal > 150000 ? Math.floor(subtotal / 10) : 0;
  const afterOffer = subtotal - offer;
  const delivery = subtotal === 0 || afterOffer > 99900 ? 0 : 6000;
  return { subtotal, offer, delivery, total: afterOffer + delivery };
}
