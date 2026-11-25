// Checks catalogue items from the WhatsApp import before the store trusts them.

export interface Item {
  id: string;
  name: string;
  paise: number;
  stock: number;
}

/** Either the checked value, or a message saying what was wrong. */
export type ParseResult<T> = { ok: true; value: T } | { ok: false; error: string };

/** true for a plain object: not null, not an array, not a string or number. */
export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isCount(value: unknown): value is number {
  return typeof value === "number" && Number.isInteger(value) && value >= 0;
}

/**
 * Checks one item. The first problem found, in this order, is the error:
 *   not a plain object                       -> "not an object"
 *   id is not text like DC-1042              -> "id must be text like DC-1042"
 *   name is not text, or is empty or spaces  -> "name must be text"
 *   paise is not a whole number of 0 or more -> "paise must be a whole number of 0 or more"
 *   stock is not a whole number of 0 or more -> "stock must be a whole number of 0 or more"
 * A good item gives { ok: true, value } with exactly the four fields: extra fields are left out.
 */
export function parseItem(value: unknown): ParseResult<Item> {
  if (!isRecord(value)) {
    return { ok: false, error: "not an object" };
  }
  const { id, name, paise, stock } = value;
  if (typeof id !== "string" || !/^DC-\d+$/.test(id)) {
    return { ok: false, error: "id must be text like DC-1042" };
  }
  if (typeof name !== "string" || name.trim() === "") {
    return { ok: false, error: "name must be text" };
  }
  if (!isCount(paise)) {
    return { ok: false, error: "paise must be a whole number of 0 or more" };
  }
  if (!isCount(stock)) {
    return { ok: false, error: "stock must be a whole number of 0 or more" };
  }
  return { ok: true, value: { id, name, paise, stock } };
}

/**
 * Checks a whole import file (JSON text holding a list of items):
 *   not JSON at all       -> "not valid JSON"
 *   JSON, but not a list  -> "expected a list of items"
 *   a bad item            -> "item 2: paise must be a whole number of 0 or more" (counting from 1)
 * All good gives { ok: true, value } with every item, in order.
 */
export function parseItems(json: string): ParseResult<Item[]> {
  let data: unknown;
  try {
    data = JSON.parse(json);
  } catch {
    return { ok: false, error: "not valid JSON" };
  }
  if (!Array.isArray(data)) {
    return { ok: false, error: "expected a list of items" };
  }
  const items: Item[] = [];
  for (let i = 0; i < data.length; i++) {
    const result = parseItem(data[i]);
    if (!result.ok) {
      return { ok: false, error: `item ${i + 1}: ${result.error}` };
    }
    items.push(result.value);
  }
  return { ok: true, value: items };
}
