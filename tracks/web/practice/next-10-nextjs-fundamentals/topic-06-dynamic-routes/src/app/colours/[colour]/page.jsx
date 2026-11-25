import Link from "next/link";
import { notFound } from "next/navigation";
import { getItems } from "../../../lib/catalogue.js";
import { COLOURS, colourBySlug } from "../../../lib/colours.js";
import { rupees } from "../../../lib/money.js";

/** The colour pages to build ahead of time. */
export function generateStaticParams() {
  // TODO 1: return one { colour } object per colour in COLOURS, using its slug.
  return [];
}

/** The page at /colours/<slug>, like /colours/indigo. */
export default async function ColourPage({ params, searchParams }) {
  // TODO 2: await params, find the colour with colourBySlug, and call notFound() when there's none.
  // TODO 3: keep the items in that colour. If (await searchParams).sort is "price", sort a copy, cheapest first.
  // TODO 4: draw the <h1>, the Cheapest first link and the list, or the "Nothing in ..." line (see README.md).
  return null;
}
