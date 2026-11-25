import type { CategoryFilter, Product } from "./types.ts";

/** Everything dhaaga.in sells at launch. Prices are in paise. */
export const PRODUCTS: readonly Product[] = [
  { id: "DC-1042", code: "AMI", name: "Golu the elephant", category: "Toys", paise: 45000 },
  { id: "DC-1101", code: "CST", name: "Marigold coaster set", category: "Home", paise: 15000 },
  { id: "DC-1102", code: "CST", name: "Indigo coaster set", category: "Home", paise: 15000 },
  { id: "DC-1201", code: "KEY", name: "Mint keychain", category: "Accessories", paise: 12000 },
  { id: "DC-1301", code: "BAG", name: "Rani pink tote bag", category: "Accessories", paise: 89900 },
  { id: "DC-1401", code: "BTY", name: "Off-white baby booties", category: "Baby", paise: 35000 },
];

/** The choices in the Category filter, in order. */
export const CATEGORY_FILTERS: readonly CategoryFilter[] = ["All", "Toys", "Home", "Accessories", "Baby"];

/** True when text is one of the Category filter's choices. */
export function isCategoryFilter(text: string): text is CategoryFilter {
  return CATEGORY_FILTERS.some((choice) => choice === text);
}
