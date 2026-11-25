// The typed price calculator. Every amount is whole paise: ₹1 is 100 paise.
import { ITEM_CODES, PRICES, GIFT_WRAP, NAME_STITCHED, DELIVERY_FEE, FREE_DELIVERY_ABOVE, DIWALI_ABOVE } from "./prices.ts";
import type { ItemCode } from "./prices.ts";
import type { CartLine, Delivery, Order, Quote } from "./types.ts";
import { formatRupees } from "./money.ts";

export function parseCode(text: string): ItemCode | null {
  const cleaned = text.trim().toUpperCase();
  return ITEM_CODES.find((code) => code === cleaned) ?? null;
}

export function itemsTotal(lines: CartLine[]): number {
  let total = 0;
  for (const line of lines) {
    total += PRICES[line.code] * line.qty;
  }
  return total;
}

export function extrasTotal(lines: CartLine[]): number {
  let total = 0;
  for (const line of lines) {
    const wrap = line.giftWrap ? GIFT_WRAP : 0;
    const name = line.name ? NAME_STITCHED : 0;
    total += (wrap + name) * line.qty;
  }
  return total;
}

export function diwaliDiscount(subtotal: number): number {
  return subtotal > DIWALI_ABOVE ? Math.floor(subtotal / 10) : 0;
}

export function deliveryFee(subtotal: number, delivery: Delivery): number {
  if (delivery === "pickup") {
    return 0;
  }
  return subtotal > FREE_DELIVERY_ABOVE ? 0 : DELIVERY_FEE;
}

export function quote(order: Order): Quote {
  const items = itemsTotal(order.lines);
  const extras = extrasTotal(order.lines);
  const subtotal = items + extras;
  const discount = diwaliDiscount(subtotal);
  const delivery = deliveryFee(subtotal, order.delivery);
  return { items, extras, subtotal, discount, delivery, total: subtotal - discount + delivery };
}

export function receipt(order: Order): string {
  const q = quote(order);
  const rows: string[] = ["Dhaaga & Co. price quote"];
  for (const line of order.lines) {
    rows.push(`${line.code} x ${line.qty}: ${formatRupees(PRICES[line.code] * line.qty)}`);
  }
  rows.push(`Extras: ${formatRupees(q.extras)}`);
  rows.push(`Subtotal: ${formatRupees(q.subtotal)}`);
  if (q.discount > 0) {
    rows.push(`Diwali offer: -${formatRupees(q.discount)}`);
  }
  if (order.delivery === "pickup") {
    rows.push("Delivery: Pickup at the stall");
  } else {
    rows.push(`Delivery: ${q.delivery === 0 ? "FREE" : formatRupees(q.delivery)}`);
  }
  rows.push(`Total: ${formatRupees(q.total)}`);
  rows.push(`Payment: ${order.payment}`);
  return rows.join("\n");
}
