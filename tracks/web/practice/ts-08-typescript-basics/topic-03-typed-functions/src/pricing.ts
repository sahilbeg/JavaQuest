// Typed pricing helpers. Every amount is whole paise: ₹1 is 100 paise.

export const GIFT_WRAP = 3000; // ₹30
export const NAME_STITCHED = 5000; // ₹50

export function lineTotal(paise: number, qty: number): number {
  // TODO 1: return the price of one item times the quantity
  return 0;
}

export function withExtras(paise: number, giftWrap?: boolean, name?: string): number {
  // TODO 2: start from paise, add GIFT_WRAP when giftWrap is true, and NAME_STITCHED when a name is given (not empty)
  return 0;
}

export function splitBill(totalPaise: number, people: number = 2): number {
  // TODO 3: each person's share, rounded down to whole paise. With no people given, split between 2.
  return 0;
}

export function sumAll(...amounts: number[]): number {
  // TODO 4: add up every amount passed in; with none, the sum is 0
  return 0;
}

export function applyOffer(paise: number, offer: (paise: number) => number): number {
  // TODO 5: offer(paise) says how many paise come off. Return the price after that, but never below 0.
  return 0;
}
