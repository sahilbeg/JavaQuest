import { getItems, getStock } from "../../lib/api.js";
import { rupees } from "../../lib/money.js";

export default async function ItemsPage() {
  const [items, stock] = await Promise.all([getItems(), getStock()]);

  return (
    <main>
      <h1>Catalogue</h1>
      {items.length === 0 ? (
        <p>No items yet. Check back soon!</p>
      ) : (
        <ul>
          {items.map((item) => {
            const left = stock[item.id] ?? 0;
            const label = left === 0 ? "Sold out" : `${left} left`;
            return <li key={item.id}>{`${item.name}: ${rupees(item.paise)} (${label})`}</li>;
          })}
        </ul>
      )}
    </main>
  );
}
