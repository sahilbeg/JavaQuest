import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { listItems } from "../lib/catalogue.js";
import { formatRupees } from "../lib/money.js";

// Round 3, the Next.js round: "The home page lists the catalogue, two items per page, at /?page=2 and so on."
const PAGE_SIZE = 2;

/** The page number from ?page=, or null when it isn't a whole number of 1 or more. No page means page 1. */
function readPage(raw) {
  if (raw === undefined) return 1;
  if (typeof raw !== "string" || !/^\d+$/.test(raw)) return null;
  const page = Number(raw);
  return page >= 1 ? page : null;
}

export async function generateMetadata({ searchParams }) {
  const page = readPage((await searchParams).page);
  if (page === null) {
    return { title: "Page not found", robots: { index: false } };
  }
  if (page === 1) {
    return { title: "Catalogue", alternates: { canonical: "/" } };
  }
  return { title: `Catalogue, page ${page}`, alternates: { canonical: `/?page=${page}` } };
}

export default async function CataloguePage({ searchParams }) {
  const raw = (await searchParams).page;
  if (raw === "1") {
    redirect("/");
  }
  const page = readPage(raw);
  const items = await listItems();
  const pageCount = Math.ceil(items.length / PAGE_SIZE);
  if (page === null || page > pageCount) {
    notFound();
  }
  const shown = items.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  return (
    <main>
      <h1>Catalogue</h1>
      <ul>
        {shown.map((item) => (
          <li key={item.id}>
            <Link href={`/items/${item.id}`}>{item.name}</Link> {formatRupees(item.paise)}
          </li>
        ))}
      </ul>
      <nav aria-label="Pages">
        {page > 1 && <Link href={page === 2 ? "/" : `/?page=${page - 1}`}>Previous</Link>}
        <p>
          Page {page} of {pageCount}
        </p>
        {page < pageCount && <Link href={`/?page=${page + 1}`}>Next</Link>}
      </nav>
    </main>
  );
}
