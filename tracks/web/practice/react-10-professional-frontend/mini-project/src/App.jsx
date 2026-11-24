import { useReducer, useState } from "react";
import { Cart } from "./Cart.jsx";
import { cartReducer } from "./cart.js";
import { Catalogue } from "./Catalogue.jsx";
import { PRODUCTS } from "./catalogue.js";
import { Checkout } from "./Checkout.jsx";
import { Footer } from "./Footer.jsx";
import { Header } from "./Header.jsx";

/** The whole store: header, then the shop or the checkout, then the footer. */
export function App({ products = PRODUCTS, nextOrderId = "order-17" }) {
  // TODO 10: keep the cart lines with useReducer(cartReducer, []) and the view ("shop" or "checkout") with useState.
  // The header counts every item (the sum of qty). Catalogue's onAdd and Cart's onSetQty and onRemove dispatch actions.
  // Checkout replaces the shop: Cart's onCheckout opens it, its onBack goes back to the shop, and its onPlaced clears the cart.
  const lines = [];
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
