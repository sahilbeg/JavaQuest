import { useState } from "react";
import { rupees } from "./money.js";

// TODO 1: The card keeps its own count, so nobody else can see it. Remove this state.
//         Take inCart and onAdd as props instead: show inCart, and call onAdd(product.id) on a click.
export function ProductCard({ product }) {
  const [inCart, setInCart] = useState(0);

  return (
    <article className="product-card">
      <h3>{product.name}</h3>
      <p className="price">{rupees(product.paise)}</p>
      <p className="in-cart">In cart: {inCart}</p>
      <button type="button" aria-label={`Add ${product.name} to cart`} onClick={() => setInCart(inCart + 1)}>
        Add to cart
      </button>
    </article>
  );
}

// Finished: shows what it is given.
export function CartSummary({ count, totalPaise, onClear }) {
  return (
    <aside className="cart-summary">
      <p className="cart-count">Items in cart: {count}</p>
      <p className="cart-total">Total: {rupees(totalPaise)}</p>
      <button type="button" onClick={onClear} disabled={count === 0}>
        Clear cart
      </button>
    </aside>
  );
}

export function Shop({ products }) {
  // TODO 2: Keep the cart here, in state: an object from product id to quantity, starting as {}.
  // TODO 3: Write addToCart(id), which adds one of that product. Make a new object; never change the old one.
  // TODO 4: Work out count (all the quantities added up) and totalPaise (each price times its quantity) from the cart.
  const count = 0;
  const totalPaise = 0;

  // TODO 5: Clear cart should empty the cart. Pass each card its inCart (0 when missing) and onAdd.
  return (
    <main className="shop">
      <CartSummary count={count} totalPaise={totalPaise} onClear={() => {}} />
      {products.map((p) => (
        <ProductCard key={p.id} product={p} />
      ))}
    </main>
  );
}
