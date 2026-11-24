// Finished: shows whole paise as rupees, for example 45000 -> "₹450.00".
const rupees = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export function formatRupees(paise) {
  return rupees.format(paise / 100);
}
