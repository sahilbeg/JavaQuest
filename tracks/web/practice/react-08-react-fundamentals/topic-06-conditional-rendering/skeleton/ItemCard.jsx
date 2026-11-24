import { rupees } from "./money.js";

// TODO 1: Choose what the badge shows:
//   stock 0      -> <span className="badge sold-out">Sold out</span>
//   stock 1 to 3 -> <span className="badge low">Only {stock} left</span>
//   more than 3  -> nothing (return null)
export function StockBadge({ stock }) {
  return null;
}

export function ItemCard({ name, paise, stock, giftWrap = false, onAdd, onNotify }) {
  return (
    <article className="item-card">
      <h3>{name}</h3>
      <p className="price">{rupees(paise)}</p>
      {/* TODO 2: Show <StockBadge /> here, passing it the stock. */}
      {/* TODO 3: Show <p className="gift-wrap">Gift wrap available</p> only when giftWrap is true. */}
      {/* TODO 4: Show this button only while stock is above 0. Otherwise show a Notify me button that calls onNotify. */}
      <button type="button" onClick={onAdd}>
        Add to cart
      </button>
    </article>
  );
}
