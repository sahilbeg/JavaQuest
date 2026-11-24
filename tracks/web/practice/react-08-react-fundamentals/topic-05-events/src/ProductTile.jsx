import { rupees } from "./money.js";

export function CartButton({ id, onAdd }) {
  function handleClick(event) {
    // TODO 1: Call onAdd with this product's id.
    // TODO 2: Stop the click from bubbling up to the tile around this button.
  }

  return (
    <button type="button" onClick={handleClick}>
      Add to cart
    </button>
  );
}

// TODO 3: Clicking anywhere on the tile should call onOpen with this tile's id. Add an onClick to the <article>.
// TODO 4: After the price, show a <CartButton /> and pass it this tile's id and onAdd.
export function ProductTile({ id, name, paise, onOpen, onAdd }) {
  return (
    <article className="product-tile">
      <h3>{name}</h3>
      <p className="price">{rupees(paise)}</p>
    </article>
  );
}

export function SearchBar({ onSearch }) {
  function handleKeyDown(event) {
    // TODO 5: When the key pressed is "Enter", call onSearch with the text in the box (event.target.value).
  }

  return <input type="search" aria-label="Search the store" onKeyDown={handleKeyDown} />;
}
