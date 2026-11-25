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
  if (!isRecord(data)) return null;
  const { id, name, price_paise, stock } = data;
  if (typeof id !== "string" || id === "") return null;
  if (typeof name !== "string") return null;
  if (!isCount(price_paise) || !isCount(stock)) return null;
  return { id, name, paise: price_paise, stock };
}

/** Checks a whole list. One bad item makes the whole list null. */
export function parseItems(data: unknown): Item[] | null {
  if (!Array.isArray(data)) return null;
  const items: Item[] = [];
  for (const entry of data) {
    const item = parseItem(entry);
    if (item === null) return null;
    items.push(item);
  }
  return items;
}

/** GETs the item list and never throws: every outcome is a Result. */
export async function fetchItems(url: string): Promise<Result<Item[]>> {
  try {
    const response = await fetch(url);
    if (!response.ok) return { ok: false, error: `HTTP ${response.status}` };
    const data: unknown = await response.json();
    const items = parseItems(data);
    if (items === null) return { ok: false, error: "The server sent items we don't understand." };
    return { ok: true, value: items };
  } catch (error) {
    return { ok: false, error: error instanceof Error ? error.message : "The network failed." };
  }
}

/** The body for POST /api/orders, in the server's names. */
export function orderRequest(lines: readonly CartLine[], phone: string): ApiOrderRequest {
  return {
    items: lines.filter((line) => line.qty > 0).map((line) => ({ item_id: line.id, qty: line.qty })),
    phone: phone.replace(/\s/g, ""),
  };
}
