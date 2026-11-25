"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { COLOURS } from "../../lib/colours.js";
import { shopHref } from "../../lib/query.js";

/** The colour and sort menus and the search box above the shop list. Each choice changes the address. */
export function Filters({ colour, q, sort }) {
  const router = useRouter();
  const [text, setText] = useState(q);

  function go(change) {
    router.push(shopHref({ colour, q, sort, ...change }));
  }

  function handleSearch(event) {
    event.preventDefault();
    go({ q: text.trim() });
  }

  return (
    <div>
      <label htmlFor="colour">Colour</label>
      <select id="colour" value={colour} onChange={(event) => go({ colour: event.target.value })}>
        <option value="">All colours</option>
        {COLOURS.map((option) => (
          <option key={option.slug} value={option.slug}>
            {option.name}
          </option>
        ))}
      </select>

      <label htmlFor="sort">Sort</label>
      <select id="sort" value={sort} onChange={(event) => go({ sort: event.target.value })}>
        <option value="">Catalogue order</option>
        <option value="price-asc">Cheapest first</option>
        <option value="price-desc">Dearest first</option>
      </select>

      <form role="search" onSubmit={handleSearch}>
        <label htmlFor="q">Search</label>
        <input id="q" value={text} onChange={(event) => setText(event.target.value)} />
        <button type="submit">Search</button>
      </form>
    </div>
  );
}
