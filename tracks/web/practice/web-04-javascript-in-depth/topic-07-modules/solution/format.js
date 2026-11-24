// Shows paise as rupees. Its one export is the default, rupees.

const formatter = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export default function rupees(paise) {
  return formatter.format(paise / 100);
}
