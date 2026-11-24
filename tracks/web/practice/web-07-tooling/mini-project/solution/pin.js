/** Is this a Pune PIN code? Six digits starting with 411, like "411038" or "411 038". */
export function isPunePin(pin) {
  if (typeof pin !== "string") return false;
  return /^411 ?\d{3}$/.test(pin.trim());
}
