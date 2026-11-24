import { useCartContext } from "./CartContext.jsx";

export function Header() {
  const { count } = useCartContext();

  return (
    <header>
      <h1>Dhaaga &amp; Co.</h1>
      <p>Cart ({count})</p>
    </header>
  );
}
