// Money helpers. In code, money is always whole paise: ₹1 = 100 paise.

const rupees = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

/** Whole paise as rupees text: 129900 -> "₹1,299.00". */
export function formatRupees(paise) {
  // TODO 1: throw a TypeError when paise is not a whole number (Number.isInteger).
  //         Otherwise return rupees.format(paise / 100).
  return "";
}

/** Typed rupees as whole paise: "₹1,299.50" -> 129950. */
export function paiseFromRupees(text) {
  // TODO 2: throw a TypeError when text is not a string.
  // TODO 3: trim it, remove a leading "₹" (and spaces after it) and every ",".
  //         Match the rest with /^(\d+)(?:\.(\d{1,2}))?$/, or throw a RangeError.
  //         Return rupees * 100 plus the decimals padded to two digits.
  return 0;
}
