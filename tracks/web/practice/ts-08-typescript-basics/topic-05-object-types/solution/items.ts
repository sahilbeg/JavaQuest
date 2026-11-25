// Items in the catalogue, typed with interfaces. Every amount is whole paise: ₹1 is 100 paise.

export interface Item {
  readonly id: string;
  name: string;
  paise: number;
  colours: string[];
}

export interface Amigurumi extends Item {
  animal: string;
}

export interface OrderLine {
  item: Item;
  qty: number;
}

export function makeItem(id: string, name: string, paise: number, colours: string[]): Item {
  return { id, name, paise, colours };
}

export function describeItem(item: Item): string {
  return `${item.id} ${item.name}: ₹${(item.paise / 100).toFixed(2)}`;
}

export function describeToy(toy: Amigurumi): string {
  return `${describeItem(toy)} (${toy.animal})`;
}

export function inColour(items: Item[], colour: string): Item[] {
  return items.filter((item) => item.colours.includes(colour));
}

export function orderTotal(lines: OrderLine[]): number {
  let total = 0;
  for (const line of lines) {
    total += line.item.paise * line.qty;
  }
  return total;
}
