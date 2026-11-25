import Link from "next/link";
import { listItems } from "../lib/store.js";
import { formatRupees } from "../lib/money.js";

// The catalogue at /: a server component that reads the store directly.
export default async function HomePage() {
  const items = await listItems();
  return (
    <>
      <h1>Dhaaga & Co.</h1>
      <ul>
        {items.map((item) => (
          <li key={item.id}>
            <Link href={`/items/${item.id}`}>{item.name}</Link>
            <p>{formatRupees(item.paise)}</p>
            {item.stock === 0 && <p>Sold out</p>}
          </li>
        ))}
      </ul>
    </>
  );
}
