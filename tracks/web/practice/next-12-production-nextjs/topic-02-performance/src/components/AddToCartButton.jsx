"use client";

import { useState } from "react";

// The only part of the item page that runs in the browser. It needs just the item's id and its stock.
export function AddToCartButton({ id, stock }) {
  const [added, setAdded] = useState(false);
  if (stock === 0) {
    return (
      <button type="button" disabled>
        Sold out
      </button>
    );
  }
  function add() {
    window.dispatchEvent(new CustomEvent("cart:add", { detail: { id } }));
    setAdded(true);
  }
  return (
    <button type="button" onClick={add}>
      {added ? "Added to cart" : "Add to cart"}
    </button>
  );
}
