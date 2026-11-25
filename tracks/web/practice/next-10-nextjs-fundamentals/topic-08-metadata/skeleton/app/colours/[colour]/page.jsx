import Link from "next/link";
import { notFound } from "next/navigation";
import { getItems } from "../../../lib/catalogue.js";
import { COLOURS, colourBySlug } from "../../../lib/colours.js";
import { rupees } from "../../../lib/money.js";

/** The line about a colour for its description, like "2 handmade pieces in Indigo, from ₹450.00." */
export function describeColour(name, items) {
  // TODO 2: none, one, or many items (see README.md). Many: the count, "pieces", and the cheapest price.
  return "";
}

/** The colour page's title, description and preview card. */
export async function generateMetadata({ params }) {
  // TODO 3: await params and find the colour. Unknown: { title: "Colour not found" }.
  // Otherwise: title "<Name> crochet", description from describeColour, and openGraph with url "/colours/<slug>".
  return {};
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
