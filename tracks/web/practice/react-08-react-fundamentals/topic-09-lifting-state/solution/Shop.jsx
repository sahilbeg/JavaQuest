import { useState } from "react";
import { rupees } from "./money.js";

export function ProductCard({ product, inCart, onAdd }) {
  return (
    <article className="product-card">
      <h3>{product.name}</h3>
      <p className="price">{rupees(product.paise)}</p>
      <p className="in-cart">In cart: {inCart}</p>
      <button type="button" aria-label={`Add ${product.name} to cart`} onClick={() => onAdd(product.id)}>
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
  const [cart, setCart] = useState({});

  function addToCart(id) {
    setCart((old) => ({ ...old, [id]: (old[id] ?? 0) + 1 }));
  }

  const count = Object.values(cart).reduce((sum, qty) => sum + qty, 0);
  const totalPaise = products.reduce((sum, p) => sum + p.paise * (cart[p.id] ?? 0), 0);

  return (
    <main className="shop">
      <CartSummary count={count} totalPaise={totalPaise} onClear={() => setCart({})} />
      {products.map((p) => (
        <ProductCard key={p.id} product={p} inCart={cart[p.id] ?? 0} onAdd={addToCart} />
      ))}
    </main>
  );
}
