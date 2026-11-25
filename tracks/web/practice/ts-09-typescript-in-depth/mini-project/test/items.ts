import type { Item } from "../src/catalogue.ts";

/** A fresh copy of the test catalogue: 8 items, in catalogue order. */
export function catalogue(): Item[] {
  return [
    { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 },
    { id: "DC-1043", code: "CST", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 0 },
    { id: "DC-1044", code: "KEY", name: "Mint keychain", colour: "Mint", paise: 12000, stock: 8 },
    { id: "DC-1045", code: "BAG", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 2 },
    { id: "DC-1046", code: "AMI", name: "Rani pink parrot", colour: "Rani pink", paise: 45000, stock: 1 },
    { id: "DC-1047", code: "BTY", name: "Mint baby booties", colour: "Mint", paise: 35000, stock: 0 },
    { id: "DC-1048", code: "BAG", name: "Marigold tote bag", colour: "Marigold", paise: 89900, stock: 0 },
    { id: "DC-1049", code: "CST", name: "Off-white coaster set", colour: "Off-white", paise: 15000, stock: 5 },
  ];
}

/** The ids of a list of items, for easy comparing. */
export function ids(items: Item[]): string[] {
  return items.map((item) => item.id);
}
