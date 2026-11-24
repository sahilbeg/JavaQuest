import { useCartContext } from "./CartContext.jsx";

export function AddButton({ item }) {
  const { add } = useCartContext();

  return <button onClick={() => add(item)}>Add {item.name}</button>;
}
