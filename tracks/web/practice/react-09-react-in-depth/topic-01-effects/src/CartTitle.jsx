import { useEffect } from "react";

/** Shows how many things are in the cart, and puts the count in the browser tab's title. */
export function CartTitle({ count }) {
  // TODO 1: add an effect that sets document.title to "(3) Dhaaga & Co." for 3 items, or "Dhaaga & Co." for none. Run it only when count changes.

  return <p>{count === 1 ? "1 item in your cart" : `${count} items in your cart`}</p>;
}
