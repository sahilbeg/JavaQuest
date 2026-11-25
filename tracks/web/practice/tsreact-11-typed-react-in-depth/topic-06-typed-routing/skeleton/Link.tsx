import type { ReactNode } from "react";
import { toHash } from "./routes.ts";
import type { Route } from "./routes.ts";

/** A link to one of the store's routes. Only routes that exist can be written. */
export function Link({ to, children }: { to: Route; children: ReactNode }) {
  return <a href={toHash(to)}>{children}</a>;
}
