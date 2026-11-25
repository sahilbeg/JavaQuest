// The shapes of an order and its quote. This file only holds types.
import type { ItemCode } from "./prices.ts";

export type PaymentMode = "CASH" | "UPI" | "CARD";
export type Delivery = "pickup" | "pune";

export interface CartLine {
  code: ItemCode;
  qty: number;
  giftWrap?: boolean;
  name?: string;
}

export interface Order {
  lines: CartLine[];
  delivery: Delivery;
  payment: PaymentMode;
}

export interface Quote {
  items: number;
  extras: number;
  subtotal: number;
  discount: number;
  delivery: number;
  total: number;
}
