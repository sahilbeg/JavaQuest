import Link from "next/link";
import { getItems } from "../../lib/catalogue.js";
import { queryCatalogue } from "../../lib/query.js";
import { rupees } from "../../lib/money.js";
import { Filters } from "./Filters.jsx";

export const metadata = {
  title: "Shop",
  description: "Every handmade piece from Dhaaga & Co., Pune.",
};

/** The shop at /items, filtered by the address: /items?colour=indigo&q=golu&sort=price-asc. */
export default async function ShopPage({ searchParams }) {
  const { colour = "", q = "", sort = "" } = await searchParams;
  const all = await getItems();
  const items = queryCatalogue(all, { colour, q, sort });

  return (
    <>
      <h1>Shop</h1>
      <Filters colour={colour} q={q} sort={sort} />
      <p>
        Showing {items.length} of {all.length} pieces
      </p>
      {items.length === 0 ? (
        <>
          <p>Nothing matches.</p>
          <Link href="/items">See everything</Link>
        </>
      ) : (
        <ul>
          {items.map((item) => (
            <li key={item.id}>
              <Link href={`/items/${item.id}`}>{item.name}</Link> {rupees(item.paise)}
              {item.stock === 0 && <span> Sold out</span>}
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
