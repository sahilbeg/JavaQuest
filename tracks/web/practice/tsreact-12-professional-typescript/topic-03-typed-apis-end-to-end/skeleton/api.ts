/** One item as the stock API sends it. The server's JSON uses snake_case names. */
export interface ApiItem {
  id: string;
  name: string;
  price_paise: number;
  stock: number;
}

/** One item as the app uses it. */
export interface Item {
  id: string;
  name: string;
  paise: number;
  stock: number;
}

/** A cart line in the app. */
export interface CartLine {
  id: string;
  qty: number;
}

/** The body the orders API expects. */
export interface ApiOrderRequest {
  items: { item_id: string; qty: number }[];
  phone: string;
}

/** Either a value, or an error message for the customer. */
export type Result<T> = { ok: true; value: T } | { ok: false; error: string };

/** True for a plain object, whose fields we can then look at one by one. */
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

/** True for a whole number that is 0 or more. */
function isCount(value: unknown): value is number {
  return typeof value === "number" && Number.isInteger(value) && value >= 0;
}

/** Checks one item from the server and turns it into an Item, or gives null when anything is wrong. */
export function parseItem(data: unknown): Item | null {
  // TODO 1: return null unless data is a record with a non-empty string id, a string name, and whole numbers of 0 or more for price_paise and stock.
  // Then return a new Item with only id, name, paise (from price_paise) and stock. isRecord and isCount above will help.
  return null;
}

/** Checks a whole list. One bad item makes the whole list null. */
export function parseItems(data: unknown): Item[] | null {
  // TODO 2: return null unless data is an array. Parse every entry with parseItem; if any gives null, return null. Otherwise return the Items.
  return null;
}

/** GETs the item list and never throws: every outcome is a Result. */
export async function fetchItems(url: string): Promise<Result<Item[]>> {
  // TODO 3: fetch(url). Not ok gives { ok: false, error: "HTTP <status>" }. Read the JSON as unknown and check it with parseItems:
  // null gives { ok: false, error: "The server sent items we don't understand." }, a list gives { ok: true, value }.
  // If fetch or json() throws, give { ok: false, error } with the Error's message.
  return { ok: false, error: "" };
}

/** The body for POST /api/orders, in the server's names. */
export function orderRequest(lines: readonly CartLine[], phone: string): ApiOrderRequest {
  // TODO 4: one { item_id, qty } per line whose qty is above 0, and the phone number with every space removed.
  return { items: [], phone: "" };
}
