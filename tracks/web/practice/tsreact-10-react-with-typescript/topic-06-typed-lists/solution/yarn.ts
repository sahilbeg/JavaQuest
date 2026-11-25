// Finished helper: the yarn types and the Aundh shop's shelf.
export type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

export interface Yarn {
  id: string;
  colour: Colour;
  grams: number;
}

/** A small amigurumi uses about 40 g, so a cone with less than this is low. */
export const LOW_GRAMS = 40;

export const SHELF: readonly Yarn[] = [
  { id: "yarn-1", colour: "Marigold", grams: 120 },
  { id: "yarn-2", colour: "Mint", grams: 30 },
  { id: "yarn-3", colour: "Indigo", grams: 260 },
  { id: "yarn-4", colour: "Mint", grams: 75 },
  { id: "yarn-5", colour: "Rani pink", grams: 15 },
];
