import Link from "next/link";
import { getItems } from "../../lib/catalogue.js";
import { rupees } from "../../lib/money.js";

/** The shop at /items. It's finished: while it awaits the catalogue, loading.jsx shows instead. */
export default async function ShopPage() {
  const items = await getItems();
  return (
    <>
      <h1>Shop</h1>
      <ul>
        {items.map((item) => (
          <li key={item.id}>
            <Link href={`/items/${item.id}`}>{item.name}</Link> {rupees(item.paise)}
          </li>
        ))}
      </ul>
    </>
  );
}
