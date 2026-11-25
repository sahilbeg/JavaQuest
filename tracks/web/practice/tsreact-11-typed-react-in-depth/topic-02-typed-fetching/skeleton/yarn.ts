/** Dhaaga's five yarn colours. */
export type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

/** What GET /api/yarn/<colour> answers: { "colour": "Marigold", "grams": 1200 }. */
export interface YarnStock {
  colour: string;
  grams: number;
}

/** A small amigurumi uses about 40 g of yarn. */
export const GRAMS_PER_AMIGURUMI = 40;

/** Loading, failed, or ready with the data: exactly one at a time. */
export type Load<T> =
  | { status: "loading" }
  | { status: "error"; message: string }
  | { status: "ready"; data: T };

/** True only when value really has the shape of a YarnStock. */
export function isYarnStock(value: unknown): value is YarnStock {
  // TODO 1: return true only for an object (not null) whose colour is a string and whose grams is a number.
  return false;
}
