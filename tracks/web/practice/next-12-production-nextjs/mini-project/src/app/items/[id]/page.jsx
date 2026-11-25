import { notFound } from "next/navigation";
import { getItem } from "../../../lib/store.js";
import { formatRupees } from "../../../lib/money.js";
import { AddToCartButton } from "../../../components/AddToCartButton.jsx";

export async function generateMetadata({ params }) {
  // TODO 4 (also): { title: item.name, description: item.description, alternates: { canonical: "/items/<id>" } },
  // or { title: "Item not found", robots: { index: false } } for an unknown id.
  return {};
}

// The page at /items/DC-1042.
export default async function ItemPage({ params }) {
  // TODO 4: await params, get the item, and call notFound() when there's none. Show the name in an <h1>, the price,
  // the description, "7 in stock" (or "Out of stock" at 0), and <AddToCartButton id={...} stock={...} />.
  return (
    <article>
      <h1>Coming soon</h1>
    </article>
  );
}
