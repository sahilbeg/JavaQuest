import { createContext, useContext } from "react";
import { useCart } from "./useCart.js";

export const CartContext = createContext(null);

/** Shares one cart with everything inside it. */
export function CartProvider({ children }) {
  const cart = useCart();
  return <CartContext value={cart}>{children}</CartContext>;
}

/** The shared cart from the nearest CartProvider above. */
export function useCartContext() {
  const cart = useContext(CartContext);
  if (cart === null) {
    throw new Error("useCartContext must be used inside <CartProvider>");
  }
  return cart;
}
