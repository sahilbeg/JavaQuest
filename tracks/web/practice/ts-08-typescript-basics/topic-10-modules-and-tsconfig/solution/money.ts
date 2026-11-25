// Showing money. Every amount is whole paise: ₹1 is 100 paise.

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export function formatRupees(paise: number): string {
  return inr.format(paise / 100);
}
