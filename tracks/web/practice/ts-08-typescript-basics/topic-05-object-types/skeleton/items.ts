// Items in the catalogue, typed with interfaces. Every amount is whole paise: ₹1 is 100 paise.

export interface Item {
  readonly id: string;
  name: string;
  paise: number;
  // TODO 1: add colours, the list of yarn colour names the item is made in (an array of strings)
}

export interface Amigurumi extends Item {
  animal: string;
}

export interface OrderLine {
  item: Item;
  qty: number;
}

export function makeItem(id: string, name: string, paise: number, colours: string[]): Item {
  // TODO 2: return an Item built from the four arguments (do TODO 1 first)
  return { id: "", name: "", paise: 0 };
}

export function describeItem(item: Item): string {
  // TODO 3: "DC-1042 Golu the elephant: ₹450.00", with the price in rupees and two decimals
  return "";
}

export function describeToy(toy: Amigurumi): string {
  // TODO 4: the same text as describeItem, then the animal in brackets: "DC-1042 Golu the elephant: ₹450.00 (elephant)"
  return "";
}

export function inColour(items: Item[], colour: string): Item[] {
  // TODO 5: the items made in that colour, in the same order
  return [];
}

export function orderTotal(lines: OrderLine[]): number {
  // TODO 6: add up the item's price times the quantity for every line
  return 0;
}
