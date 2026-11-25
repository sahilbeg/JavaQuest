"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";

/** The search box in the header. */
export function SearchBox() {
  const router = useRouter();
  const [text, setText] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    // TODO 4: trim text. Nothing typed: router.push("/items").
    // Otherwise: router.push("/items?q=" + the text encoded with encodeURIComponent).
  }

  return (
    <form role="search" onSubmit={handleSubmit}>
      <label htmlFor="q">Search the shop</label>
      <input id="q" value={text} onChange={(event) => setText(event.target.value)} />
      <button type="submit">Search</button>
    </form>
  );
}
