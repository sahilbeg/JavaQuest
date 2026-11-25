import Link from "next/link";
import { notFound } from "next/navigation";
import { getItems } from "../../../lib/catalogue.js";
import { rupees } from "../../../lib/money.js";

// TODO 1 (bronze): every item page shows the same item. Each page must show the item its address asks for.
// TODO 2 (silver): an id that isn't in the catalogue must show the not-found page, never another item.
// TODO 3 (gold): the preview must name the right item, with the title, description and openGraph from README.md.

/** Finds the item the address asks for. */
async function findItem(params) {
  const items = await getItems();
  return items.find((item) => item.id.startsWith(params.id)) ?? items[0];
}

/** The tab title, description and WhatsApp preview for an item's page. */
export async function generateMetadata({ params }) {
  const item = await findItem(params);
  return {
    title: `${item.name} | Dhaaga & Co.`,
    description: `${item.name}, handmade in Pune. ${rupees(item.paise)}.`,
  };
}

/** The page at /items/<id>, like /items/DC-1042. */
export default async function ItemPage({ params }) {
  const item = await findItem(params);
  return (
    <article>
      <h1>{item.name}</h1>
      <p>{rupees(item.paise)}</p>
      <p>{item.stock > 0 ? `${item.stock} left` : "Sold out"}</p>
      <Link href="/items">Back to the shop</Link>
    </article>
  );
}
