"use client";
import { useState } from "react";

/** The Add to cart button in each row of the home page. */
export function AddToCartButton({ itemId, name }) {
  const [count, setCount] = useState(0);
  return (
    <>
      <button type="button" onClick={() => setCount(count + 1)}>
        Add to cart
      </button>
      {count > 0 && <p>{count} in your cart</p>}
    </>
  );
}
