import { useEffect, useState } from "react";
import { parseRoute, pathFromHash } from "./routes.ts";
import type { Route } from "./routes.ts";

/** The route for the address the browser shows now. The app renders again when it changes. */
export function useRoute(): Route {
  const [path, setPath] = useState<string>(() => pathFromHash(window.location.hash));

  useEffect(() => {
    function handleChange() {
      setPath(pathFromHash(window.location.hash));
    }
    window.addEventListener("hashchange", handleChange);
    return () => window.removeEventListener("hashchange", handleChange);
  }, []);

  return parseRoute(path);
}
