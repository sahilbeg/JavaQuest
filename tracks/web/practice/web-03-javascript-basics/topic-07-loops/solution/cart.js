// Loops for the WhatsApp basket. Prices are whole paise.

export function sumPaise(prices) {
  let total = 0;
  for (const price of prices) {
    total = total + price;
  }
  return total;
}

export function countAbove(prices, limit) {
  let count = 0;
  for (const price of prices) {
    if (price > limit) {
      count = count + 1;
    }
  }
  return count;
}

export function toysFromYarn(grams) {
  let toys = 0;
  while (grams >= 40) {
    grams = grams - 40;
    toys = toys + 1;
  }
  return toys;
}

export function firstIndexOver(prices, limit) {
  for (let i = 0; i < prices.length; i++) {
    if (prices[i] > limit) {
      return i;
    }
  }
  return -1;
}
