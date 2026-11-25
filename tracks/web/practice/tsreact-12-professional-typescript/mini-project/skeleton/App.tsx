import { useReducer, useState } from "react";
import { Cart } from "./Cart.tsx";
import { cartReducer } from "./cart.ts";
import { Catalogue } from "./Catalogue.tsx";
import { PRODUCTS } from "./catalogue.ts";
import { Checkout } from "./Checkout.tsx";
import { Footer } from "./Footer.tsx";
import { Header } from "./Header.tsx";
import type { CartLine, Product } from "./types.ts";

type View = "shop" | "checkout";

const EMPTY_CART: CartLine[] = [];

export interface AppProps {
  products?: readonly Product[];
  nextOrderId?: string;
}

/** The whole store: header, then the shop or the checkout, then the footer. */
export function App({ products = PRODUCTS, nextOrderId = "order-17" }: AppProps) {
  // TODO 10: keep the cart with useReducer(cartReducer, EMPTY_CART) and the view (a View, starting at "shop") with useState.
  // The header counts every item (the sum of qty). Catalogue's onAdd and Cart's onSetQty and onRemove dispatch actions.
  // Checkout replaces the shop: Cart's onCheckout opens it, its onBack goes back to the shop, and its onPlaced clears the cart.
  const lines = EMPTY_CART;
  const count = 0;

  return (
    <>
      <Header cartCount={count} />
      <main id="main" tabIndex={-1}>
        <div className="shop">
          <Catalogue products={products} onAdd={() => {}} />
          <Cart lines={lines} onSetQty={() => {}} onRemove={() => {}} onCheckout={() => {}} />
        </div>
      </main>
      <Footer />
    </>
  );
}
