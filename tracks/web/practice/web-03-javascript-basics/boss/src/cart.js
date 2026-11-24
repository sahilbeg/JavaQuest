// The old WhatsApp price calculator. It keeps every amount in rupees, as decimals.
// INC-03: big orders come out a paisa short. Make it work in whole paise.

// TODO 1 (bronze): store every price in whole paise, like CST: 15000
export const PRICES = { CST: 150, KEY: 120, AMI: 450, BAG: 899, BTY: 350 };

// Turns an amount typed in the old sheet, in rupees, into paise.
export function toPaise(rupees) {
  // TODO 2 (bronze): Math.floor loses a paisa when rupees * 100 lands just below a whole number. Round to the nearest.
  return Math.floor(rupees * 100);
}

// The Diwali offer: 10% off orders above ₹1,500, rounded down to whole paise.
export function diwaliDiscount(subtotalPaise) {
  // TODO 3 (silver): exactly ₹1,500 gets no offer, and the discount must round down, never up
  if (subtotalPaise >= 150000) {
    return Math.round(subtotalPaise * 0.1);
  }
  return 0;
}

// The order total in paise: every line's price times its quantity, minus the Diwali offer.
export function cartTotal(lines) {
  // TODO 4 (bronze): add up whole paise, then take off diwaliDiscount(subtotal). No rupee decimals anywhere.
  let total = 0;
  for (const line of lines) {
    total = total + PRICES[line.code] * line.qty;
  }
  if (total > 1500) {
    total = total - total * 0.1;
  }
  return toPaise(total);
}

// The total as text for the receipt.
export function formatRupees(paise) {
  // TODO 5 (gold): show it like "₹2,068.20", with Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" })
  return "₹" + paise / 100;
}

export function totalLine(lines) {
  return "Total: " + formatRupees(cartTotal(lines));
}
