// Types shared by every file of the price calculator. This file only holds types, so it is erased completely.

export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

export interface CartLine {
  code: ItemCode;
  qty: number;
}

// The settings a script writes into tsconfig.json for every Dhaaga project.
export interface CompilerOptions {
  strict: boolean;
  noEmit: boolean;
  target: string;
  module: string;
  moduleResolution: string;
}
