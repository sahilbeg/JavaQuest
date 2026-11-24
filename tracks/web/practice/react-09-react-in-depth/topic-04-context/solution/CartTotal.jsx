import { useCartContext } from "./CartContext.jsx";
import { rupees } from "./money.js";

export function CartTotal() {
  const { total } = useCartContext();

  return <p>Total: {rupees(total)}</p>;
}
