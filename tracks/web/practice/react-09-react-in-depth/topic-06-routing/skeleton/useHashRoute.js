import { useEffect, useState } from "react";
import { currentPath } from "./router.js";

/** The current path, kept in step with the address bar. */
export function useHashRoute() {
  // TODO 4: keep the path in state, starting from currentPath().
  const path = currentPath();

  // TODO 5: in an effect, listen for "hashchange" on window and set the path to currentPath(). Remove the listener in the cleanup.

  return path;
}
