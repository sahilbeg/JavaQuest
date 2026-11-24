// Yes-or-no checks for the checkout. Every function returns true or false.

export function isFreeDelivery(subtotalPaise) {
  return subtotalPaise > 99900;
}

export function isWholePaise(value) {
  return Number.isInteger(value);
}

export function sameQuantity(typed, qty) {
  return Number(typed) === qty;
}

export function isBlankNote(note) {
  return note.trim() === "";
}
