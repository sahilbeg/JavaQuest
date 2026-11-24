import { useLayoutEffect, useRef, useState } from "react";
import { rupees } from "./money.js";

/** A product card that stacks its parts when it is narrower than 400 pixels. */
export function ProductCard({ item }) {
  const cardRef = useRef(null);
  const [layout, setLayout] = useState("row");

  useLayoutEffect(() => {
    const width = cardRef.current.getBoundingClientRect().width;
    setLayout(width < 400 ? "stack" : "row");
  }, []);

  return (
    <article
      ref={cardRef}
      data-layout={layout}
      style={{ display: "flex", flexDirection: layout === "stack" ? "column" : "row", gap: "1rem" }}
    >
      <h3>{item.name}</h3>
      <p>{rupees(item.paise)}</p>
    </article>
  );
}
