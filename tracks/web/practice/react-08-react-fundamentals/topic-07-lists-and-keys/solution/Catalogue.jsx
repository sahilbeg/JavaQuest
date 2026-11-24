import { rupees } from "./money.js";

export function ProductList({ products }) {
  if (products.length === 0) {
    return <p className="empty">Nothing here yet.</p>;
  }

  return (
    <ul className="product-list">
      {products.map((p) => (
        <li key={p.id}>
          <span className="name">{p.name}</span> <span className="price">{rupees(p.paise)}</span>
        </li>
      ))}
    </ul>
  );
}

export function GiftsUnder({ products, maxPaise }) {
  const gifts = products.filter((p) => p.paise < maxPaise).sort((a, b) => a.paise - b.paise);

  return (
    <section className="gifts">
      <h2>Gifts under {rupees(maxPaise)}</h2>
      <ProductList products={gifts} />
    </section>
  );
}
