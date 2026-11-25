import { getItems, getStock } from "../../lib/api.js";
import { rupees } from "../../lib/money.js";

export default async function ItemsPage() {
  // TODO 3: get the items and the stock at the same time:
  // const [items, stock] = await Promise.all([...]);
  const items = [];
  const stock = {};

  // TODO 4: under the heading, show one <li key={item.id}> per item, like "Golu the elephant: ₹450.00 (7 left)".
  // An item with 0 left (or missing from stock) says "(Sold out)". Use rupees(item.paise) for the price.
  // With no items at all, show <p>No items yet. Check back soon!</p> instead of the list.
  return (
    <main>
      <h1>Catalogue</h1>
    </main>
  );
}
