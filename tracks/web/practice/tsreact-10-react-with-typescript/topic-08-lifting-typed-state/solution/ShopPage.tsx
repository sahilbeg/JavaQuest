import { useState } from "react";
import { CartSummary } from "./CartSummary.tsx";
import { ItemList } from "./ItemList.tsx";
import type { CartLine, Item } from "./items.ts";

export interface ShopPageProps {
  items: readonly Item[];
}

export function ShopPage({ items }: ShopPageProps) {
  const [cart, setCart] = useState<CartLine[]>([]);

  function add(item: Item) {
    if (cart.some((line) => line.item.id === item.id)) {
      setCart(cart.map((line) => (line.item.id === item.id ? { ...line, qty: line.qty + 1 } : line)));
    } else {
      setCart([...cart, { item, qty: 1 }]);
    }
  }

  function remove(id: string) {
    setCart(cart.filter((line) => line.item.id !== id));
  }

  const count = cart.reduce((sum, line) => sum + line.qty, 0);

  return (
    <div className="shop">
      <header className="shop-header">
        <h1>Dhaaga & Co.</h1>
        <p className="cart-badge">Cart ({count})</p>
      </header>
      <ItemList items={items} onAdd={add} />
      <CartSummary lines={cart} onRemove={remove} />
    </div>
  );
}
