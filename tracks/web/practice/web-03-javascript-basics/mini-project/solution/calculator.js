// FEAT-03: the price and delivery calculator. Every amount is whole paise.
// A cart is an array of lines like { code: "BAG", qty: 2, giftWrap: true, name: "Riya" }.
// giftWrap and name are optional: a line without them has no extras.

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };
export const GIFT_WRAP = 3000;
export const NAME_STITCHED = 5000;
export const DELIVERY = 6000;

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export function formatRupees(paise) {
  return inr.format(paise / 100);
}

export function itemsTotal(lines) {
  let total = 0;
  for (const line of lines) {
    total = total + PRICES[line.code] * line.qty;
  }
  return total;
}

export function extrasTotal(lines) {
  let total = 0;
  for (const line of lines) {
    let perItem = 0;
    if (line.giftWrap) {
      perItem = perItem + GIFT_WRAP;
    }
    if (line.name) {
      perItem = perItem + NAME_STITCHED;
    }
    total = total + perItem * line.qty;
  }
  return total;
}

export function diwaliDiscount(subtotal) {
  return subtotal > 150000 ? Math.floor(subtotal / 10) : 0;
}

export function deliveryFee(subtotal) {
  return subtotal > 99900 ? 0 : DELIVERY;
}

export function quote(lines) {
  const items = itemsTotal(lines);
  const extras = extrasTotal(lines);
  const subtotal = items + extras;
  const discount = diwaliDiscount(subtotal);
  const delivery = deliveryFee(subtotal);
  const total = subtotal - discount + delivery;
  return { items, extras, subtotal, discount, delivery, total };
}

export function receipt(lines) {
  const q = quote(lines);
  const rows = ["Dhaaga & Co. price quote"];
  for (const line of lines) {
    rows.push(`${line.code} x ${line.qty}: ${formatRupees(PRICES[line.code] * line.qty)}`);
  }
  rows.push(`Extras: ${formatRupees(q.extras)}`);
  rows.push(`Subtotal: ${formatRupees(q.subtotal)}`);
  if (q.discount > 0) {
    rows.push(`Diwali offer: -${formatRupees(q.discount)}`);
  }
  rows.push(`Delivery: ${q.delivery === 0 ? "FREE" : formatRupees(q.delivery)}`);
  rows.push(`Total: ${formatRupees(q.total)}`);
  return rows.join("\n");
}
