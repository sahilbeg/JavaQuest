/** What the cart costs, in paise: { items, delivery, total }. */
export function checkoutTotals(lines) {
  const items = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const delivery = items === 0 || items > 99900 ? 0 : 6000;
  return { items, delivery, total: items + delivery };
}
