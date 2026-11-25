// The team's compiler settings. A script writes them into tsconfig.json for every Dhaaga project.
import type { CompilerOptions } from "./types.ts";

export const TEAM_OPTIONS: CompilerOptions = {
  // TODO 5: the team's settings: strict on, noEmit on (Vite builds, tsc only checks), target "es2022",
  //         module "esnext" and moduleResolution "bundler"
  strict: false,
  noEmit: false,
  target: "es2015",
  module: "commonjs",
  moduleResolution: "node10",
};
