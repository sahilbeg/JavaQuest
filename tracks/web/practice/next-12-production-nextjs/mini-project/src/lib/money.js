/** Whole paise to rupees for people: 45000 becomes "₹450.00", 161910 becomes "₹1,619.10". */
export function formatRupees(paise) {
  return "₹" + (paise / 100).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
