// Orders with optional details. Every amount is whole paise: ₹1 is 100 paise.

export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

export type OrderLine = {
  code: ItemCode;
  qty: number;
  giftWrap?: boolean;
  name?: string;
};

export type Address = {
  area: string;
  city: string;
  pin?: string;
};

export type Customer = {
  name: string;
  phone?: string;
  address?: Address;
};

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };
export const GIFT_WRAP = 3000; // ₹30 per item
export const NAME_STITCHED = 5000; // ₹50 per item

export function extrasFor(line: OrderLine): number {
  // TODO 1: per item, GIFT_WRAP when giftWrap is true and NAME_STITCHED when there is a name; times the quantity
  return 0;
}

export function lineTotal(line: OrderLine): number {
  // TODO 2: the item's price from PRICES times the quantity, plus extrasFor(line)
  return 0;
}

export function cityOf(customer: Customer): string {
  // TODO 3: the customer's city, or "Pickup at the stall" when there is no address
  return "";
}

export function pinOf(customer: Customer): string {
  // TODO 4: the address's PIN code, or "PIN missing" when there is no address or no PIN
  return "";
}

export function qtyOrOne(qty?: number): number {
  // TODO 5: the quantity given, or 1 when it was left out. A quantity of 0 (a cancelled line) stays 0.
  return -1;
}
