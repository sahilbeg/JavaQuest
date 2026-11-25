// Showing money. Every amount is whole paise: ₹1 is 100 paise.

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export function formatRupees(paise: number): string {
  // TODO 1: show paise as rupees with inr.format, the Indian way: 129900 gives "₹1,299.00"
  return "";
}
