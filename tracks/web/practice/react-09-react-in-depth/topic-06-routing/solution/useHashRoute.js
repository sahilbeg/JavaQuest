import { useEffect, useState } from "react";
import { currentPath } from "./router.js";

/** The current path, kept in step with the address bar. */
export function useHashRoute() {
  const [path, setPath] = useState(currentPath);

  useEffect(() => {
    function onChange() {
      setPath(currentPath());
    }
    window.addEventListener("hashchange", onChange);
    return () => window.removeEventListener("hashchange", onChange);
  }, []);

  return path;
}
