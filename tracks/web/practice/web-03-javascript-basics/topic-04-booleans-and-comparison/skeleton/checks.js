// Yes-or-no checks for the checkout. Every function returns true or false.

export function isFreeDelivery(subtotalPaise) {
  // TODO 1: return true when the order is above ₹999 (99900 paise). Exactly ₹999 still pays for delivery.
  return false;
}

export function isWholePaise(value) {
  // TODO 2: return true only for a whole number, like 15000. Text like "15000" and 150.5 give false.
  return false;
}

export function sameQuantity(typed, qty) {
  // TODO 3: typed is text from a form, like "2". Turn it into a number, then compare it with qty using ===.
  return false;
}

export function isBlankNote(note) {
  // TODO 4: return true when the gift note is empty or only spaces. Trim it, then compare it with "".
  return false;
}
