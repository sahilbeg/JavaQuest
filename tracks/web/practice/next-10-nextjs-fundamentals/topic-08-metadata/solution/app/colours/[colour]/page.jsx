import Link from "next/link";
import { notFound } from "next/navigation";
import { getItems } from "../../../lib/catalogue.js";
import { COLOURS, colourBySlug } from "../../../lib/colours.js";
import { rupees } from "../../../lib/money.js";

/** The line about a colour for its description, like "2 handmade pieces in Indigo, from ₹450.00." */
export function describeColour(name, items) {
  if (items.length === 0) return `No pieces in ${name} right now.`;
  const cheapest = Math.min(...items.map((item) => item.paise));
  const pieces = items.length === 1 ? "piece" : "pieces";
  return `${items.length} handmade ${pieces} in ${name}, from ${rupees(cheapest)}.`;
}

/** The colour page's title, description and preview card. */
export async function generateMetadata({ params }) {
  const { colour: slug } = await params;
  const colour = colourBySlug(slug);
  if (!colour) return { title: "Colour not found" };

  const items = (await getItems()).filter((item) => item.colour === colour.name);
  const title = `${colour.name} crochet`;
  const description = describeColour(colour.name, items);
  return { title, description, openGraph: { title, description, url: `/colours/${colour.slug}` } };
}

/** The colour pages to build ahead of time. */
export function generateStaticParams() {
  return COLOURS.map((colour) => ({ colour: colour.slug }));
}

/** The page at /colours/<slug>, like /colours/indigo. It's finished. */
export default async function ColourPage({ params, searchParams }) {
  const { colour: slug } = await params;
  const colour = colourBySlug(slug);
  if (!colour) notFound();

  const { sort } = await searchParams;
  let items = (await getItems()).filter((item) => item.colour === colour.name);
  if (sort === "price") items = [...items].sort((a, b) => a.paise - b.paise);

  return (
    <>
      <h1>{colour.name}</h1>
      {items.length === 0 ? (
        <p>Nothing in {colour.name} right now.</p>
      ) : (
        <>
          <Link href={`/colours/${colour.slug}?sort=price`}>Cheapest first</Link>
          <ul>
            {items.map((item) => (
              <li key={item.id}>
                <Link href={`/items/${item.id}`}>{item.name}</Link> {rupees(item.paise)}
              </li>
            ))}
          </ul>
        </>
      )}
    </>
  );
}
