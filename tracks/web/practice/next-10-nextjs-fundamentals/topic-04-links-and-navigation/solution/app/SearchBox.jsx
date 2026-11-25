"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";

/** The search box in the header. */
export function SearchBox() {
  const router = useRouter();
  const [text, setText] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    const q = text.trim();
    router.push(q ? "/items?q=" + encodeURIComponent(q) : "/items");
  }

  return (
    <form role="search" onSubmit={handleSubmit}>
      <label htmlFor="q">Search the shop</label>
      <input id="q" value={text} onChange={(event) => setText(event.target.value)} />
      <button type="submit">Search</button>
    </form>
  );
}
