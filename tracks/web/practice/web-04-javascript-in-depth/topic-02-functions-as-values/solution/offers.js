// Small tools for the catalogue that take functions, or give them back.

export const GIFT_WRAP_PAISE = 3000;

export function countWhere(items, test) {
  let count = 0;
  for (const item of items) {
    if (test(item)) {
      count = count + 1;
    }
  }
  return count;
}

export function applyToAll(items, change) {
  const results = [];
  for (const item of items) {
    results.push(change(item));
  }
  return results;
}

export function priceAtMost(maxPaise) {
  return (item) => item.paise <= maxPaise;
}

export function withGiftWrap(priceOf) {
  return (item) => priceOf(item) + GIFT_WRAP_PAISE;
}
