// The team's compiler settings. A script writes them into tsconfig.json for every Dhaaga project.
import type { CompilerOptions } from "./types.ts";

export const TEAM_OPTIONS: CompilerOptions = {
  strict: true,
  noEmit: true,
  target: "es2022",
  module: "esnext",
  moduleResolution: "bundler",
};
