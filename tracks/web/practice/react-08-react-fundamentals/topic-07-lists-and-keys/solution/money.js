// Finished helper: turns whole paise into rupees for the page.
// rupees(45000) is "₹450.00" and rupees(129900) is "₹1,299.00".
export function rupees(paise) {
  return "₹" + (paise / 100).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
