// Small tools for the catalogue that take functions, or give them back.

export const GIFT_WRAP_PAISE = 3000;

export function countWhere(items, test) {
  let count = 0;
  // TODO 1: loop over items and add 1 to count each time test(item) is true
  return count;
}

export function applyToAll(items, change) {
  const results = [];
  // TODO 2: loop over items and push change(item) into results
  return results;
}

export function priceAtMost(maxPaise) {
  // TODO 3: return an arrow function that takes an item and checks item.paise <= maxPaise
  return (item) => false;
}

export function withGiftWrap(priceOf) {
  // TODO 4: return an arrow function that takes an item and returns priceOf(item) + GIFT_WRAP_PAISE
  return (item) => 0;
}
