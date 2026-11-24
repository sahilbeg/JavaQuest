import { CartProvider } from "./CartContext.jsx";
import { Header } from "./Header.jsx";
import { AddButton } from "./AddButton.jsx";
import { CartTotal } from "./CartTotal.jsx";
import { ITEMS } from "./items.js";
import { rupees } from "./money.js";

/** The whole shop. Notice that no component below passes the cart as a prop. */
export function Shop() {
  return (
    <CartProvider>
      <Header />
      <main>
        <Shelf />
        <CartTotal />
      </main>
    </CartProvider>
  );
}

function Shelf() {
  return (
    <section>
      <h2>Bestsellers</h2>
      {ITEMS.map((item) => (
        <ProductCard key={item.code} item={item} />
      ))}
    </section>
  );
}

function ProductCard({ item }) {
  return (
    <article>
      <h3>{item.name}</h3>
      <p>{rupees(item.paise)}</p>
      <AddButton item={item} />
    </article>
  );
}
