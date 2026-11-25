import { notFound } from "next/navigation";
import { getItem } from "../../../lib/catalogue.js";
import { formatRupees } from "../../../lib/money.js";

// The <title>, description and sharing preview for one item's page.
export async function generateMetadata({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) {
    return { title: "Item not found", robots: { index: false } };
  }
  return {
    title: item.name,
    description: item.description,
    alternates: { canonical: `/items/${item.id}` },
    openGraph: {
      title: item.name,
      description: item.description,
      images: [{ url: `/images/${item.id}.jpg`, width: 1200, height: 630, alt: item.alt }],
    },
  };
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
