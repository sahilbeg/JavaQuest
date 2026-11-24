// Money helpers. In code, money is always whole paise: ₹1 = 100 paise.

const rupees = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

/** Whole paise as rupees text: 129900 -> "₹1,299.00". */
export function formatRupees(paise) {
  if (!Number.isInteger(paise)) {
    throw new TypeError(`formatRupees needs whole paise, not ${JSON.stringify(paise)}`);
  }
  return rupees.format(paise / 100);
}

/** Typed rupees as whole paise: "₹1,299.50" -> 129950. */
export function paiseFromRupees(text) {
  if (typeof text !== "string") {
    throw new TypeError("paiseFromRupees needs text, like \"₹1,299.50\"");
  }
  const cleaned = text.trim().replace(/^₹\s*/, "").replaceAll(",", "");
  const match = /^(\d+)(?:\.(\d{1,2}))?$/.exec(cleaned);
  if (match === null) {
    throw new RangeError(`"${text}" is not an amount in rupees`);
  }
  const [, whole, decimals = ""] = match;
  return Number(whole) * 100 + Number(decimals.padEnd(2, "0"));
}
