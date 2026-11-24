import { useRef, useState } from "react";

/** The shop's search box, with buttons that put the cursor straight into it. */
export function SearchBar() {
  // TODO 1: make a ref with useRef(null), and attach it to the input with ref={...}.
  const [query, setQuery] = useState("");

  function search() {
    // TODO 2: move the focus into the input.
  }

  function clear() {
    setQuery("");
    // TODO 3: move the focus back into the input.
  }

  return (
    <div role="search">
      <label htmlFor="search">Search the shop</label>
      <input id="search" value={query} onChange={(e) => setQuery(e.target.value)} />
      <button type="button" onClick={search}>
        Search
      </button>
      <button type="button" onClick={clear}>
        Clear
      </button>
    </div>
  );
}
