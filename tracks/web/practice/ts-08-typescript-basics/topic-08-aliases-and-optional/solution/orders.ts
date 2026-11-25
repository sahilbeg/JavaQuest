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
  const wrap = line.giftWrap ? GIFT_WRAP : 0;
  const name = line.name ? NAME_STITCHED : 0;
  return (wrap + name) * line.qty;
}

export function lineTotal(line: OrderLine): number {
  return PRICES[line.code] * line.qty + extrasFor(line);
}

export function cityOf(customer: Customer): string {
  return customer.address?.city ?? "Pickup at the stall";
}

export function pinOf(customer: Customer): string {
  return customer.address?.pin ?? "PIN missing";
}

export function qtyOrOne(qty?: number): number {
  return qty ?? 1;
}
