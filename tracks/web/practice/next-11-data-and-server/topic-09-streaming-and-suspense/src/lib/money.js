/** Paise as rupees for people: rupees(45000) is "₹450.00", rupees(129900) is "₹1,299.00". */
export function rupees(paise) {
  return "₹" + (paise / 100).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
