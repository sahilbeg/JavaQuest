// Small functions for the extras. Every amount is whole paise.
// Gift wrap is 3000 paise and a stitched name is 5000 paise, for each item.

export function lineTotal(paise, qty) {
  // TODO 1: give qty a default of 1, then return paise times qty
  return 0;
}

export function extrasFor(giftWrap, name) {
  // TODO 2: return the extras for one item: 3000 when giftWrap is true, plus 5000 when name has text.
  // name may be "" or left out (undefined): then there is no name to stitch.
  return 0;
}

export function itemCost(paise, qty, giftWrap, name) {
  // TODO 3: call lineTotal and extrasFor. Extras are for each item, so multiply them by qty too.
  return 0;
}

// TODO 4: make this arrow function return 0 above 99900 paise, and 6000 otherwise
export const deliveryFor = (subtotalPaise) => 0;
