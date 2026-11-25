// The cart, with typed arrays and tuples. Every amount is whole paise: ₹1 is 100 paise.

// Each entry is a tuple: [item code, price in paise].
export const PRICE_LIST: readonly [code: string, paise: number][] = [
  ["CST", 15000],
  ["KEY", 12000],
  ["AMI", 45000],
  ["BAG", 89900],
  ["BTY", 35000],
];

export function priceOf(code: string): number {
  // TODO 1: return the price of the entry in PRICE_LIST whose code matches, or 0 when no entry matches
  return 0;
}

export function cartTotal(lines: [code: string, qty: number][]): number {
  // TODO 2: add up price times quantity for every [code, qty] line, using priceOf
  return 0;
}

export function codesInCart(lines: [code: string, qty: number][]): string[] {
  // TODO 3: the codes in the cart, each one once, in the order they first appear
  return [];
}

export function priceRange(prices: readonly number[]): [number, number] {
  // TODO 4: [lowest, highest] of the prices; [0, 0] when there are none
  return [0, 0];
}

export function cheapestFirst(list: readonly [code: string, paise: number][]): [code: string, paise: number][] {
  // TODO 5: a new list sorted from the lowest price to the highest. Don't change the list you were given.
  return [];
}
