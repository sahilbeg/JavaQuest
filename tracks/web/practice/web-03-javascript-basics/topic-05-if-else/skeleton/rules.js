// Rules the website follows. Days are "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" and "Sun". Hours are 0 to 23.

export function deliveryFee(subtotalPaise, pickup) {
  // TODO 1: a pickup at the stall costs 0. Otherwise delivery is 6000 paise, but it's free above 99900 paise.
  return 0;
}

export function stallOpen(day, hour) {
  // TODO 2: the stall opens on "Sat" and "Sun", from 10:00 until 20:00. Return true when it is open.
  return false;
}

export function noteLine(note) {
  // TODO 3: if note has text, return "Note: " followed by the note. For "", undefined or null, return "No note".
  return "";
}

export function priceBand(paise) {
  // TODO 4: below 20000 return "under ₹200", below 50000 return "₹200 to ₹499", otherwise "₹500 and up"
  return "";
}
