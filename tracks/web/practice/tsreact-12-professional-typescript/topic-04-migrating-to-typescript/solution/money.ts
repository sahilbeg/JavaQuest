/** Paise to rupees for people: 45000 gives "₹450.00", 129900 gives "₹1,299.00". */
export function formatRupees(paise: number): string {
  return "₹" + (paise / 100).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
