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
  const [lines, dispatch] = useReducer(cartReducer, []);
  const [view, setView] = useState("shop");
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
