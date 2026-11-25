// Finished helper: the five things Dhaaga sells, by code.
export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

export const ITEM_NAMES: Record<ItemCode, string> = {
  CST: "Coaster set (4)",
  KEY: "Keychain",
  AMI: "Amigurumi",
  BAG: "Tote bag",
  BTY: "Baby booties",
};

/** True when text is one of the five codes, so TypeScript treats it as an ItemCode afterwards. */
export function isItemCode(text: string): text is ItemCode {
  return Object.hasOwn(ITEM_NAMES, text);
}
