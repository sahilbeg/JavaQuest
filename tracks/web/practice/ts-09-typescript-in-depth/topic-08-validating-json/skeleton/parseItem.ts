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
  // TODO 1: check typeof, null and Array.isArray.
  return false;
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
  // TODO 2: use isRecord, then check id, name, paise and stock in that order.
  // TODO 3: build the value from the checked fields only.
  return { ok: false, error: "" };
}

/**
 * Checks a whole import file (JSON text holding a list of items):
 *   not JSON at all       -> "not valid JSON"
 *   JSON, but not a list  -> "expected a list of items"
 *   a bad item            -> "item 2: paise must be a whole number of 0 or more" (counting from 1)
 * All good gives { ok: true, value } with every item, in order.
 */
export function parseItems(json: string): ParseResult<Item[]> {
  // TODO 4: JSON.parse inside try/catch, check Array.isArray, then parseItem each element.
  return { ok: false, error: "" };
}
