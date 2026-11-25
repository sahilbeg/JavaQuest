// Finished: the catalogue's types and its six products.
export const COLOURS = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"] as const;
export type Colour = (typeof COLOURS)[number];

export type Stock = { kind: "in-stock"; count: number } | { kind: "sold-out" };

export interface Product {
  id: string;
  name: string;
  paise: number;
  colour: Colour;
  stock: Stock;
}

export const PRODUCTS: readonly Product[] = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, colour: "Indigo", stock: { kind: "in-stock", count: 2 } },
  { id: "DC-1043", name: "Marigold coaster set", paise: 15000, colour: "Marigold", stock: { kind: "in-stock", count: 12 } },
  { id: "DC-1044", name: "Mint keychain", paise: 12000, colour: "Mint", stock: { kind: "sold-out" } },
  { id: "DC-1045", name: "Indigo tote bag", paise: 89900, colour: "Indigo", stock: { kind: "in-stock", count: 5 } },
  { id: "DC-1046", name: "Rani pink baby booties", paise: 35000, colour: "Rani pink", stock: { kind: "in-stock", count: 1 } },
  { id: "DC-1047", name: "Off-white coaster set", paise: 15000, colour: "Off-white", stock: { kind: "in-stock", count: 8 } },
];
