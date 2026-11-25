import Link from "next/link";
import { notFound } from "next/navigation";
import { getItems } from "../../../lib/catalogue.js";
import { rupees } from "../../../lib/money.js";

/** Finds the item the address asks for, or null. */
async function findItem(params) {
  const { id } = await params;
  const items = await getItems();
  return items.find((item) => item.id === id) ?? null;
}

/** The tab title, description and WhatsApp preview for an item's page. */
export async function generateMetadata({ params }) {
  const item = await findItem(params);
  if (!item) return { title: "Item not found" };
  const description = `${item.name}, handmade in Pune. ${rupees(item.paise)}.`;
  return {
    title: item.name,
    description,
    openGraph: { title: item.name, description, url: `/items/${item.id}` },
  };
}

/** The page at /items/<id>, like /items/DC-1042. */
export default async function ItemPage({ params }) {
  const item = await findItem(params);
  if (!item) notFound();
  return (
    <article>
      <h1>{item.name}</h1>
      <p>{rupees(item.paise)}</p>
      <p>{item.stock > 0 ? `${item.stock} left` : "Sold out"}</p>
      <Link href="/items">Back to the shop</Link>
    </article>
  );
}
