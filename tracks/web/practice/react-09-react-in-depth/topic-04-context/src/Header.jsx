import { useCartContext } from "./CartContext.jsx";

export function Header() {
  // TODO 3: take count from the shared cart with useCartContext().
  const count = 0;

  return (
    <header>
      <h1>Dhaaga &amp; Co.</h1>
      <p>Cart ({count})</p>
    </header>
  );
}
