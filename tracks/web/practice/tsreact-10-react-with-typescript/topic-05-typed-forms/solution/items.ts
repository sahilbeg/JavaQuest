// Finished helper: the five things Dhaaga sells, with their prices in paise.
export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

export const PRICES: Record<ItemCode, number> = {
  CST: 15000,
  KEY: 12000,
  AMI: 45000,
  BAG: 89900,
  BTY: 35000,
};

export const GIFT_WRAP_PAISE = 3000;

/** True when text is one of the five codes, so TypeScript treats it as an ItemCode afterwards. */
export function isItemCode(text: string): text is ItemCode {
  return Object.hasOwn(PRICES, text);
}
