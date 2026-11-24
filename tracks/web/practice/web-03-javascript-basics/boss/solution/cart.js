// The WhatsApp price calculator, in whole paise.

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };

// Turns an amount typed in the old sheet, in rupees, into the nearest whole paisa.
export function toPaise(rupees) {
  return Math.round(rupees * 100);
}

// The Diwali offer: 10% off orders above ₹1,500, rounded down to whole paise.
export function diwaliDiscount(subtotalPaise) {
  if (subtotalPaise > 150000) {
    return Math.floor(subtotalPaise / 10);
  }
  return 0;
}

// The order total in paise: every line's price times its quantity, minus the Diwali offer.
export function cartTotal(lines) {
  let subtotal = 0;
  for (const line of lines) {
    subtotal = subtotal + PRICES[line.code] * line.qty;
  }
  return subtotal - diwaliDiscount(subtotal);
}

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

// The total as text for the receipt.
export function formatRupees(paise) {
  return inr.format(paise / 100);
}

export function totalLine(lines) {
  return "Total: " + formatRupees(cartTotal(lines));
}
