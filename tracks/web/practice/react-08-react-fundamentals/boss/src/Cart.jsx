import { useState } from "react";
import { rupees } from "./money.js";

const QUANTITIES = [1, 2, 3, 4, 5];

// One line of the cart. The customer can change the quantity with the dropdown.
export function CartRow({ item, onRemove }) {
  const [qty, setQty] = useState(item.qty);

  return (
    <li className="cart-row">
      <span className="name">{item.name}</span>
      <select aria-label={`Quantity for ${item.name}`} value={qty} onChange={(event) => setQty(Number(event.target.value))}>
        {QUANTITIES.map((n) => (
          <option key={n} value={n}>
            {n}
          </option>
        ))}
      </select>
      <span className="line-total">{rupees(item.paise * qty)}</span>
      <button type="button" aria-label={`Remove ${item.name}`} onClick={onRemove}>
        Remove
      </button>
    </li>
  );
}

// TODO 1: Customers say that removing one item changes the quantity of another. Find out why and fix it.
// TODO 2: Removing one of two items with the same name removes both. Make Remove take away only the clicked item.
export function Cart({ initialItems }) {
  const [items, setItems] = useState(initialItems);

  function removeItem(target) {
    setItems(items.filter((item) => item.name !== target.name));
  }

  if (items.length === 0) {
    return <p className="empty">Your cart is empty.</p>;
  }

  return (
    <ul className="cart">
      {items.map((item, index) => (
        <CartRow key={index} item={item} onRemove={() => removeItem(item)} />
      ))}
    </ul>
  );
}
