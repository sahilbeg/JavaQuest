// Shows paise as rupees. Its one export is the default, rupees.

// TODO 3: formatter is only for this file. Remove the word export in front of it.
export const formatter = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export default function rupees(paise) {
  // TODO 4: return formatter.format(paise / 100)
  return "";
}
