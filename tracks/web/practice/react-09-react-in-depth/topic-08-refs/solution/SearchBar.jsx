import { useRef, useState } from "react";

/** The shop's search box, with buttons that put the cursor straight into it. */
export function SearchBar() {
  const inputRef = useRef(null);
  const [query, setQuery] = useState("");

  function search() {
    inputRef.current.focus();
  }

  function clear() {
    setQuery("");
    inputRef.current.focus();
  }

  return (
    <div role="search">
      <label htmlFor="search">Search the shop</label>
      <input id="search" ref={inputRef} value={query} onChange={(e) => setQuery(e.target.value)} />
      <button type="button" onClick={search}>
        Search
      </button>
      <button type="button" onClick={clear}>
        Clear
      </button>
    </div>
  );
}
