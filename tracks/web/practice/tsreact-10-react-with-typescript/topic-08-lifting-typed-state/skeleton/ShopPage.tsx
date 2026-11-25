import { useState } from "react";
import { CartSummary } from "./CartSummary.tsx";
import { ItemList } from "./ItemList.tsx";
import type { CartLine, Item } from "./items.ts";

export interface ShopPageProps {
  items: readonly Item[];
}

export function ShopPage({ items }: ShopPageProps) {
  // TODO 1: The header, the list and the cart box all need the cart, so keep it here in state:
  //         const [cart, setCart] = useState<CartLine[]>([]);
  const cart: CartLine[] = [];

  // TODO 2: If the item is already in the cart, give that line qty + 1 (map, with a new line object).
  //         Otherwise add { item, qty: 1 } at the end of a new array.
  function add(item: Item) {}

  // TODO 3: Keep every line except the one whose item has this id.
  function remove(id: string) {}

  const count = cart.reduce((sum, line) => sum + line.qty, 0);

  return (
    <div className="shop">
      <header className="shop-header">
        <h1>Dhaaga & Co.</h1>
        <p className="cart-badge">Cart ({count})</p>
      </header>
      <ItemList items={items} onAdd={add} />
      {/* TODO 4: Pass onRemove={remove} once CartSummary takes it. */}
      <CartSummary lines={cart} />
    </div>
  );
}
