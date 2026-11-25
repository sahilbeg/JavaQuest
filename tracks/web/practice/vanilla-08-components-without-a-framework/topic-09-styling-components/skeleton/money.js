// Money is stored in paise (whole numbers). This turns it into rupees for people to read.
const formatter = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

/** 45000 paise becomes "₹450.00"; 129900 becomes "₹1,299.00". */
export function rupees(paise) {
  return formatter.format(paise / 100);
}
