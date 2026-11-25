import { useEffect, useState } from "react";
import { parseRoute, pathFromHash } from "./routes.ts";
import type { Route } from "./routes.ts";

/** The route for the address the browser shows now. The app renders again when it changes. */
export function useRoute(): Route {
  // TODO 3: keep the path in useState<string>, starting from pathFromHash(window.location.hash).
  //         Return parseRoute(path).
  // TODO 4: in an effect, listen for "hashchange" on window and set the path from window.location.hash.
  //         Return a cleanup that removes the listener.
  return parseRoute(pathFromHash(window.location.hash));
}
