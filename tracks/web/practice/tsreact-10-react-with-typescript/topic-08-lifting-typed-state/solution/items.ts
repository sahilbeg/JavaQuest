// Finished helper: the items on the shop page, and what a cart line holds.
export interface Item {
  id: string;
  name: string;
  paise: number;
}

export interface CartLine {
  item: Item;
  qty: number;
}

export const ITEMS: readonly Item[] = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1043", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1044", name: "Mint keychain", paise: 12000 },
  { id: "DC-1045", name: "Indigo tote bag", paise: 89900 },
];
