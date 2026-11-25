import { notFound } from "next/navigation";
import { getItem } from "../../../lib/catalogue.js";
import { formatRupees } from "../../../lib/money.js";

// The page at /items/DC-1042: a server component, so it can be async and read data directly.
export default async function ItemPage({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) {
    notFound();
  }
  return (
    <main>
      <h1>{item.name}</h1>
      <p>{formatRupees(item.paise)}</p>
      <p>{item.stock > 0 ? `${item.stock} in stock` : "Sold out"}</p>
    </main>
  );
}
