import { useCartContext } from "./CartContext.jsx";

export function AddButton({ item }) {
  // TODO 4: take add from the shared cart with useCartContext(), and call add(item) on click.
  function handleClick() {}

  return <button onClick={handleClick}>Add {item.name}</button>;
}
