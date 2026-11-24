// Small functions for the extras. Every amount is whole paise.
// Gift wrap is 3000 paise and a stitched name is 5000 paise, for each item.

export function lineTotal(paise, qty = 1) {
  return paise * qty;
}

export function extrasFor(giftWrap, name) {
  let extra = 0;
  if (giftWrap) {
    extra = extra + 3000;
  }
  if (name) {
    extra = extra + 5000;
  }
  return extra;
}

export function itemCost(paise, qty, giftWrap, name) {
  return lineTotal(paise, qty) + extrasFor(giftWrap, name) * qty;
}

export const deliveryFor = (subtotalPaise) => (subtotalPaise > 99900 ? 0 : 6000);
