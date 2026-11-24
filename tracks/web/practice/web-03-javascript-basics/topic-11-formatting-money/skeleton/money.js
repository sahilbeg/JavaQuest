// Showing paise as rupees. Every amount comes in as whole paise.

export function plainRupees(paise) {
  // TODO 1: turn paise into rupees with exactly two decimals, using toFixed: 15050 gives "150.50"
  return "";
}

export function formatINR(paise) {
  // TODO 2: use Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }) on the rupees: 129900 gives "₹1,299.00"
  return "";
}

export function priceTag(name, paise) {
  // TODO 3: return a tag like "Tote bag: ₹899.00", using formatINR
  return "";
}

export function receiptRow(label, paise) {
  // TODO 4: pad the label with spaces at the end to 12 characters,
  // then add formatINR(paise) padded with spaces at the start to 10 characters
  return "";
}
