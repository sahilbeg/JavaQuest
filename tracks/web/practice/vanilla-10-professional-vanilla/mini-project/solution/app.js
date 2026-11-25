import { el } from "./dom.js";
import { createStore } from "./store.js";
import { cartReducer } from "./cart.js";
import { cartTotals } from "./pricing.js";
import { mountHeader } from "./header.js";
import { mountCatalogue } from "./catalogueView.js";
import { mountCart } from "./cartView.js";
import { mountCheckout } from "./checkout.js";
import { siteFooter } from "./layout.js";

/** Builds the whole store inside root: header, main (the shop or checkout) and footer. Returns the cart store. */
export function startStore(root, products) {
  const store = createStore(cartReducer, []);
  const header = el("header", { id: "site-header" });
  const main = el("main", { id: "main", tabindex: "-1" });
  root.replaceChildren(header, main, siteFooter());
  mountHeader(header, store);

  let nextOrder = 17;
  let stopCart = () => {};

  function showShop() {
    stopCart();
    const shop = el("section", { id: "shop", "aria-labelledby": "shop-heading" });
    const cart = el("section", { id: "cart", "aria-labelledby": "cart-heading" });
    main.replaceChildren(shop, cart);
    mountCatalogue(shop, products, (product) => store.dispatch({ type: "add", product }));
    stopCart = mountCart(cart, store, showCheckout);
  }

  function showCheckout() {
    stopCart();
    stopCart = () => {};
    const section = el("section", { id: "checkout", "aria-labelledby": "checkout-heading" });
    main.replaceChildren(section);
    mountCheckout(section, {
      totalPaise: cartTotals(store.getState()).total,
      orderId: `order-${nextOrder}`,
      onPlaced: () => {
        nextOrder += 1;
        store.dispatch({ type: "clear" });
      },
      onBack: showShop,
    });
  }

  showShop();
  return store;
}
