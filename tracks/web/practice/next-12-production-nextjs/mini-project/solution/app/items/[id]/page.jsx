import { notFound } from "next/navigation";
import { getItem } from "../../../lib/store.js";
import { formatRupees } from "../../../lib/money.js";
import { AddToCartButton } from "../../../components/AddToCartButton.jsx";

export async function generateMetadata({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) {
    return { title: "Item not found", robots: { index: false } };
  }
  return { title: item.name, description: item.description, alternates: { canonical: `/items/${item.id}` } };
}

// The page at /items/DC-1042.
export default async function ItemPage({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) {
    notFound();
  }
  return (
    <article>
      <h1>{item.name}</h1>
      <p>{formatRupees(item.paise)}</p>
      <p>{item.description}</p>
      <p>{item.stock > 0 ? `${item.stock} in stock` : "Out of stock"}</p>
      <AddToCartButton id={item.id} stock={item.stock} />
    </article>
  );
}
