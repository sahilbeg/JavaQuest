import { rupees } from "./money.js";

export function ProductList({ products }) {
  // TODO 1: When products is empty, return <p className="empty">Nothing here yet.</p> instead of the list.
  // TODO 2: Inside the <ul>, make one <li> per product with map, keyed by the product's id.
  //         Each <li> holds <span className="name">{name}</span> and <span className="price">{rupees(paise)}</span>.
  return <ul className="product-list"></ul>;
}

export function GiftsUnder({ products, maxPaise }) {
  // TODO 3: Keep only the products whose paise is below maxPaise.
  // TODO 4: Sort them cheapest first, without changing the products array itself.
  const gifts = products;

  return (
    <section className="gifts">
      <h2>Gifts under {rupees(maxPaise)}</h2>
      <ProductList products={gifts} />
    </section>
  );
}
