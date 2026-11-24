import { useEffect, useState } from "react";

/**
 * Round 3. Fetches JSON from url, and fetches again when url changes.
 * Returns { status, data, error }: status is "loading", "success" or "error".
 */
export function useFetch(url) {
  const [state, setState] = useState({ status: "loading", data: null, error: null });

  useEffect(() => {
    const controller = new AbortController();
    setState({ status: "loading", data: null, error: null });
    fetch(url, { signal: controller.signal })
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
      })
      .then((data) => setState({ status: "success", data, error: null }))
      .catch((error) => {
        if (error.name === "AbortError") return;
        setState({ status: "error", data: null, error: error.message });
      });
    return () => controller.abort();
  }, [url]);

  return state;
}
