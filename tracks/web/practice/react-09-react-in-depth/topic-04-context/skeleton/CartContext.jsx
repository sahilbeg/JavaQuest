import { createContext, useContext } from "react";
import { useCart } from "./useCart.js";

export const CartContext = createContext(null);

/** Stands in for the shared cart until the provider is written, so the page still draws. */
const NO_CART = { lines: [], count: 0, total: 0, add() {}, remove() {} };

/** Shares one cart with everything inside it. */
export function CartProvider({ children }) {
  // TODO 1: call useCart() once and wrap children in <CartContext value={cart}>.
  return children;
}

/** The shared cart from the nearest CartProvider above. */
export function useCartContext() {
  // TODO 2: read the cart with useContext(CartContext). If it is null, throw new Error("useCartContext must be used inside <CartProvider>").
  return NO_CART;
}
