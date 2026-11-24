import { useCartContext } from "./CartContext.jsx";
import { rupees } from "./money.js";

export function CartTotal() {
  // TODO 5: take total from the shared cart with useCartContext().
  const total = 0;

  return <p>Total: {rupees(total)}</p>;
}
