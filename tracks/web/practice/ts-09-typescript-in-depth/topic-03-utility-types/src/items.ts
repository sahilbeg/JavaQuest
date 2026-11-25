// The catalogue's master Item type, and every other item type built from it.

export type Code = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

export interface Item {
  id: string;
  code: Code;
  name: string;
  colour: string;
  paise: number;
  stock: number;
}

/** What the edit form may send: any of these fields, but never the id or the code. */
export type ItemUpdate = Partial<Omit<Item, "id" | "code">>;

/** What a product card needs, and nothing more. */
export type ItemCard = Pick<Item, "id" | "name" | "paise">;

/** A new item before the shop gives it an id. */
export type NewItem = Omit<Item, "id">;

/** Units in stock for every item code. */
export type StockByCode = Record<Code, number>;

/** A new item with the changes applied on top. The item passed in is never changed. */
export function updateItem(item: Item, changes: ItemUpdate): Item {
  // TODO 1: return a new object: every field of item, then every field of changes on top of them.
  return item;
}

/** A card with exactly three fields: id, name and paise. */
export function toCard(item: Item): ItemCard {
  // TODO 2: build a new object with only id, name and paise.
  return { id: "", name: "", paise: 0 };
}

/** The new item with its id: "DC-" followed by nextNumber, for example DC-1050. */
export function createItem(input: NewItem, nextNumber: number): Item {
  // TODO 3: copy input and add the id.
  return { ...input, id: "" };
}

/** Units in stock for each code. Every code is there, with 0 when no item has it. */
export function stockByCode(items: Item[]): StockByCode {
  // TODO 4: start from 0 for every code, then add each item's stock to its code.
  return { CST: 0, KEY: 0, AMI: 0, BAG: 0, BTY: 0 };
}
