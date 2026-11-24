// Showing paise as rupees. Every amount comes in as whole paise.

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export function plainRupees(paise) {
  return (paise / 100).toFixed(2);
}

export function formatINR(paise) {
  return inr.format(paise / 100);
}

export function priceTag(name, paise) {
  return `${name}: ${formatINR(paise)}`;
}

export function receiptRow(label, paise) {
  return label.padEnd(12) + formatINR(paise).padStart(10);
}
