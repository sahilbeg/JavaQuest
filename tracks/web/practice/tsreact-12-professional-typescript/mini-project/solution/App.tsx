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
  const [lines, dispatch] = useReducer(cartReducer, EMPTY_CART);
  const [view, setView] = useState<View>("shop");
  const count = lines.reduce((sum, line) => sum + line.qty, 0);

  return (
    <>
      <Header cartCount={count} />
      <main id="main" tabIndex={-1}>
        {view === "shop" ? (
          <div className="shop">
            <Catalogue products={products} onAdd={(product) => dispatch({ type: "add", product })} />
            <Cart
              lines={lines}
              onSetQty={(id, qty) => dispatch({ type: "setQty", id, qty })}
              onRemove={(id) => dispatch({ type: "remove", id })}
              onCheckout={() => setView("checkout")}
            />
          </div>
        ) : (
          <Checkout
            lines={lines}
            orderId={nextOrderId}
            onPlaced={() => dispatch({ type: "clear" })}
            onBack={() => setView("shop")}
          />
        )}
      </main>
      <Footer />
    </>
  );
}
