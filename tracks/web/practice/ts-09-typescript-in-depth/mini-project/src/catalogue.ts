// The typed catalogue engine behind Sana's filter buttons.

export type Code = "CST" | "KEY" | "AMI" | "BAG" | "BTY";
export type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";
export type ItemId = `DC-${number}`;

export interface Item {
  readonly id: ItemId;
  code: Code;
  name: string;
  colour: Colour;
  paise: number;
  stock: number;
}

/** One question a customer can ask. The tag, by, says which kind. */
export type Filter =
  | { by: "colour"; colour: Colour }
  | { by: "price"; minPaise?: number; maxPaise?: number }
  | { by: "inStock" }
  | { by: "search"; text: string };

/** The only fields the catalogue can be sorted by. */
export type SortKey = keyof Pick<Item, "name" | "paise" | "stock">;

export interface Query {
  filters: Filter[];
  sort?: { key: SortKey; direction: "asc" | "desc" };
}

/** What a product card shows. */
export interface ItemCard {
  id: ItemId;
  name: string;
  price: string;
  soldOut: boolean;
}

/** ₹ with two decimals, for example ₹1,299.00. Done for you. */
export function rupees(paise: number): string {
  const [whole, fraction] = (paise / 100).toFixed(2).split(".");
  return `₹${whole.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}.${fraction}`;
}

/** Throws a clear error for a filter the engine doesn't know. Done for you. */
export function assertNever(value: never): never {
  throw new Error(`Unknown filter: ${JSON.stringify(value)}`);
}

/**
 * Whether one item passes one filter:
 *   colour  -> the item's colour is exactly that colour
 *   price   -> minPaise <= paise <= maxPaise, ends included; either may be missing, and 0 is a real limit
 *   inStock -> stock is above 0
 *   search  -> the name contains the text, ignoring capitals and spaces around the text; "" matches everything
 * Any other filter throws through assertNever.
 */
export function matches(item: Item, filter: Filter): boolean {
  // TODO 1: switch on filter.by, one case per kind, and default: return assertNever(filter).
  return false;
}

/**
 * The items that pass every filter, as a new array in catalogue order.
 * With query.sort, sorted by that key ("asc" or "desc"); items that tie keep catalogue order.
 * items is never changed.
 */
export function runQuery(items: readonly Item[], query: Query): Item[] {
  // TODO 2: filter with every filter, then sort the copy when query.sort is set.
  return [];
}

/** How many items have each value of key, in the order the values first appear. */
export function countBy<K extends keyof Item>(items: readonly Item[], key: K): Map<Item[K], number> {
  // TODO 3: count item[key] for every item in a new Map<Item[K], number>.
  return new Map<Item[K], number>();
}

/** The card for one item: its id and name, the price in rupees, and whether it's sold out. */
export function toCard(item: Item): ItemCard {
  // TODO 4: build the card. soldOut is true when stock is 0.
  return { id: item.id, name: "", price: "", soldOut: false };
}
