import type { Item } from "./items.ts";
import { rupees } from "./money.ts";

export interface ItemListProps {
  items: readonly Item[];
  onAdd: (item: Item) => void;
}

// Finished: shows each item with an Add button, and tells the parent which item was added.
export function ItemList({ items, onAdd }: ItemListProps) {
  return (
    <ul className="item-list">
      {items.map((item) => (
        <li key={item.id}>
          {item.name}, {rupees(item.paise)}{" "}
          <button type="button" aria-label={`Add ${item.name}`} onClick={() => onAdd(item)}>
            Add
          </button>
        </li>
      ))}
    </ul>
  );
}
