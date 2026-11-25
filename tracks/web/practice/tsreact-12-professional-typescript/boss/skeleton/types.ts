/** One line in a cart. Money is whole paise. */
export interface CartLine {
  id: string;
  name: string;
  paise: number;
  qty: number;
}

/** The most of one item in one order. */
export const MAX_QTY = 5;

/** True for one cart line with the right field types. */
function isCartLine(value: unknown): value is CartLine {
  return (
    typeof value === "object" &&
    value !== null &&
    "id" in value &&
    typeof value.id === "string" &&
    "name" in value &&
    typeof value.name === "string" &&
    "paise" in value &&
    Number.isInteger(value.paise) &&
    "qty" in value &&
    Number.isInteger(value.qty)
  );
}

/** True for a list of cart lines, such as a cart read back from localStorage. */
export function isCartLines(value: unknown): value is CartLine[] {
  return Array.isArray(value) && value.every(isCartLine);
}
