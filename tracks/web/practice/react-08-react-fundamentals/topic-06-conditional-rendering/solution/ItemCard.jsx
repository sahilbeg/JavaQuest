import { rupees } from "./money.js";

export function StockBadge({ stock }) {
  if (stock === 0) {
    return <span className="badge sold-out">Sold out</span>;
  }
  if (stock <= 3) {
    return <span className="badge low">Only {stock} left</span>;
  }
  return null;
}

export function ItemCard({ name, paise, stock, giftWrap = false, onAdd, onNotify }) {
  return (
    <article className="item-card">
      <h3>{name}</h3>
      <p className="price">{rupees(paise)}</p>
      <StockBadge stock={stock} />
      {giftWrap && <p className="gift-wrap">Gift wrap available</p>}
      {stock > 0 ? (
        <button type="button" onClick={onAdd}>
          Add to cart
        </button>
      ) : (
        <button type="button" onClick={onNotify}>
          Notify me
        </button>
      )}
    </article>
  );
}
