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
  return { ...item, ...changes };
}

/** A card with exactly three fields: id, name and paise. */
export function toCard(item: Item): ItemCard {
  return { id: item.id, name: item.name, paise: item.paise };
}

/** The new item with its id: "DC-" followed by nextNumber, for example DC-1050. */
export function createItem(input: NewItem, nextNumber: number): Item {
  return { ...input, id: `DC-${nextNumber}` };
}

/** Units in stock for each code. Every code is there, with 0 when no item has it. */
export function stockByCode(items: Item[]): StockByCode {
  const totals: StockByCode = { CST: 0, KEY: 0, AMI: 0, BAG: 0, BTY: 0 };
  for (const item of items) {
    totals[item.code] += item.stock;
  }
  return totals;
}
