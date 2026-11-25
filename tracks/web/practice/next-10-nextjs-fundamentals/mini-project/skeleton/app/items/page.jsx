import Link from "next/link";
import { getItems } from "../../lib/catalogue.js";
import { queryCatalogue } from "../../lib/query.js";
import { rupees } from "../../lib/money.js";
import { Filters } from "./Filters.jsx";

// TODO 3: the shop's metadata: title "Shop" and the description from README.md.
export const metadata = {};

/** The shop at /items, filtered by the address: /items?colour=indigo&q=golu&sort=price-asc. */
export default async function ShopPage({ searchParams }) {
  // TODO 4: await searchParams (colour, q and sort, each "" when missing) and query the catalogue.
  // TODO 5: draw <Filters ... />, the "Showing 2 of 6 pieces" line and the list, or "Nothing matches." (see README.md).
  return <h1>Shop</h1>;
}
