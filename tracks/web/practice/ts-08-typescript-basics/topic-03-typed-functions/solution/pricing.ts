// Typed pricing helpers. Every amount is whole paise: ₹1 is 100 paise.

export const GIFT_WRAP = 3000; // ₹30
export const NAME_STITCHED = 5000; // ₹50

export function lineTotal(paise: number, qty: number): number {
  return paise * qty;
}

export function withExtras(paise: number, giftWrap?: boolean, name?: string): number {
  let total = paise;
  if (giftWrap) {
    total += GIFT_WRAP;
  }
  if (name) {
    total += NAME_STITCHED;
  }
  return total;
}

export function splitBill(totalPaise: number, people: number = 2): number {
  return Math.floor(totalPaise / people);
}

export function sumAll(...amounts: number[]): number {
  let sum = 0;
  for (const amount of amounts) {
    sum += amount;
  }
  return sum;
}

export function applyOffer(paise: number, offer: (paise: number) => number): number {
  return Math.max(0, paise - offer(paise));
}
