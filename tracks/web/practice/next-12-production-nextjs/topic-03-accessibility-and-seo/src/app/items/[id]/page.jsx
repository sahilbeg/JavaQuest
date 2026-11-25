import { notFound } from "next/navigation";
import { getItem } from "../../../lib/catalogue.js";
import { formatRupees } from "../../../lib/money.js";

// The <title>, description and sharing preview for one item's page.
export async function generateMetadata({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  // TODO 3: for an unknown item, return { title: "Item not found", robots: { index: false } }.
  // For a real item, return its name as the title, its description, alternates.canonical "/items/<id>",
  // and openGraph with the same title and description and one image: /images/<id>.jpg, 1200 by 630, alt from the item.
  return { title: "Dhaaga & Co." };
}

export default async function ItemPage({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) {
    notFound();
  }
  return (
    <article>
      <h1>{item.name}</h1>
      <img src={`/images/${item.id}.jpg`} alt={item.alt} width={600} height={600} />
      <p>{formatRupees(item.paise)}</p>
      <p>{item.description}</p>
    </article>
  );
}
