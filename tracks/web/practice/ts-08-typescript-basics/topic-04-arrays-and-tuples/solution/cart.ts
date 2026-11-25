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
  for (const [listCode, paise] of PRICE_LIST) {
    if (listCode === code) {
      return paise;
    }
  }
  return 0;
}

export function cartTotal(lines: [code: string, qty: number][]): number {
  let total = 0;
  for (const [code, qty] of lines) {
    total += priceOf(code) * qty;
  }
  return total;
}

export function codesInCart(lines: [code: string, qty: number][]): string[] {
  const codes: string[] = [];
  for (const [code] of lines) {
    if (!codes.includes(code)) {
      codes.push(code);
    }
  }
  return codes;
}

export function priceRange(prices: readonly number[]): [number, number] {
  if (prices.length === 0) {
    return [0, 0];
  }
  return [Math.min(...prices), Math.max(...prices)];
}

export function cheapestFirst(list: readonly [code: string, paise: number][]): [code: string, paise: number][] {
  return [...list].sort((a, b) => a[1] - b[1]);
}
