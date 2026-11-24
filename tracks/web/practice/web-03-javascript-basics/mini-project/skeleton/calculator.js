// FEAT-03: the price and delivery calculator. Every amount is whole paise.
// A cart is an array of lines like { code: "BAG", qty: 2, giftWrap: true, name: "Riya" }.
// giftWrap and name are optional: a line without them has no extras.

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };
export const GIFT_WRAP = 3000;
export const NAME_STITCHED = 5000;
export const DELIVERY = 6000;

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export function formatRupees(paise) {
  // TODO 1: show paise as rupees with inr, like "₹1,299.00"
  return "";
}

export function itemsTotal(lines) {
  // TODO 2: add up the price times qty of every line, without any extras
  return 0;
}

export function extrasTotal(lines) {
  // TODO 3: for each item, gift wrap adds GIFT_WRAP when giftWrap is true,
  // and a name adds NAME_STITCHED when name has text. Extras are for every item, so remember qty.
  return 0;
}

export function diwaliDiscount(subtotal) {
  // TODO 4: 10% off when subtotal is above 150000, rounded down to whole paise. Otherwise 0.
  return 0;
}

export function deliveryFee(subtotal) {
  // TODO 5: free (0) when subtotal is above 99900, otherwise DELIVERY
  return 0;
}

export function quote(lines) {
  // TODO 6: return { items, extras, subtotal, discount, delivery, total }
  // subtotal is items + extras. total is subtotal - discount + delivery.
  return { items: 0, extras: 0, subtotal: 0, discount: 0, delivery: 0, total: 0 };
}

export function receipt(lines) {
  // TODO 7: build the quote text shown in the KB page, its rows joined with "\n"
  return "";
}
