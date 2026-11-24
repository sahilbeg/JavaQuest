// The price list. Other modules ask this one for prices.

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };

export const DELIVERY_PAISE = 6000;

export function priceOf(code) {
  return PRICES[code] ?? 0;
}
