import { useEffect } from "react";

/** Shows how many things are in the cart, and puts the count in the browser tab's title. */
export function CartTitle({ count }) {
  useEffect(() => {
    document.title = count > 0 ? `(${count}) Dhaaga & Co.` : "Dhaaga & Co.";
  }, [count]);

  return <p>{count === 1 ? "1 item in your cart" : `${count} items in your cart`}</p>;
}
