import { rupees } from "./money.js";

export function CartButton({ id, onAdd }) {
  function handleClick(event) {
    event.stopPropagation();
    onAdd(id);
  }

  return (
    <button type="button" onClick={handleClick}>
      Add to cart
    </button>
  );
}

export function ProductTile({ id, name, paise, onOpen, onAdd }) {
  return (
    <article className="product-tile" onClick={() => onOpen(id)}>
      <h3>{name}</h3>
      <p className="price">{rupees(paise)}</p>
      <CartButton id={id} onAdd={onAdd} />
    </article>
  );
}

export function SearchBar({ onSearch }) {
  function handleKeyDown(event) {
    if (event.key === "Enter") {
      onSearch(event.target.value);
    }
  }

  return <input type="search" aria-label="Search the store" onKeyDown={handleKeyDown} />;
}
