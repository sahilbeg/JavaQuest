import { useEffect, useState } from "react";

/**
 * Round 3. Fetches JSON from url, and fetches again when url changes.
 * Returns { status, data, error }: status is "loading", "success" or "error".
 *
 * This is the candidate's answer. It works on a fast, perfect network.
 */
export function useFetch(url) {
  const [data, setData] = useState(null);

  // TODO 5: keep { status, data, error } in state, and set it back to loading (data and error null) whenever url changes.
  // TODO 6: a response that is not ok becomes status "error" with error "HTTP <status>"; a failed fetch becomes "error" with the error's message.
  // TODO 7: give fetch an AbortController's signal, abort it in the effect's cleanup, and ignore the AbortError that follows.
  useEffect(() => {
    fetch(url)
      .then((response) => response.json())
      .then(setData)
      .catch(() => {});
  }, [url]);

  return { status: data ? "success" : "loading", data, error: null };
}
