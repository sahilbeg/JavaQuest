// The checkout screen. In the real store it carries the full list of PIN codes Dhaaga delivers to,
// which makes it big, so it should load only when a customer opens the checkout. This file is finished.

const PUNE_PINS = ["411001", "411004", "411007", "411038", "411045"];

export default function checkoutView() {
  const section = document.createElement("section");
  const h1 = document.createElement("h1");
  h1.textContent = "Checkout";
  const note = document.createElement("p");
  note.textContent = `We deliver to ${PUNE_PINS.length} PIN codes in Pune, and more every month.`;
  section.append(h1, note);
  return section;
}
