// The counter's order slip. An item looks like { code: "KEY", name: "Keychain", qty: 2 }.

/** A new <li class="line" data-code="..."> with a .name span, a .qty span and a Remove button. Not on the page yet. */
export function slipLine(item) {
  // TODO 1: create the li (class line, data-code), the .name and .qty spans and the .remove button, then append them to the li and return it
  return null;
}

/** Adds a line for the item at the end of the slip, and returns that line. */
export function addLine(slip, item) {
  // TODO 2: make the line with slipLine, append it to the slip, and return it
  return null;
}

/** Removes the slip's line with this data-code: true if one was removed, false if there was none. */
export function removeLine(slip, code) {
  // TODO 3: search inside the slip for [data-code="..."]; return false for null, otherwise remove it and return true
  return false;
}

/** Removes every line. The slip itself stays on the page. */
export function clearSlip(slip) {
  // TODO 4: remove all of the slip's children
}
