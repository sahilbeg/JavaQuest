import { useLayoutEffect, useRef, useState } from "react";
import { rupees } from "./money.js";

/** A product card that stacks its parts when it is narrower than 400 pixels. */
export function ProductCard({ item }) {
  const [layout, setLayout] = useState("row");

  // TODO 4: attach a ref to the <article>, and in useLayoutEffect measure its width with getBoundingClientRect().
  //         Under 400 set the layout to "stack", otherwise "row".

  return (
    <article data-layout={layout} style={{ display: "flex", flexDirection: layout === "stack" ? "column" : "row", gap: "1rem" }}>
      <h3>{item.name}</h3>
      <p>{rupees(item.paise)}</p>
    </article>
  );
}
