import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { listItems } from "../lib/catalogue.js";
import { formatRupees } from "../lib/money.js";

// Round 3, the Next.js round: "The home page lists the catalogue, two items per page, at /?page=2 and so on."
const PAGE_SIZE = 2;

export async function generateMetadata({ searchParams }) {
  // TODO 6: page 1 gives { title: "Catalogue", alternates: { canonical: "/" } }. Page 2 gives
  // { title: "Catalogue, page 2", alternates: { canonical: "/?page=2" } }. A bad page gives
  // { title: "Page not found", robots: { index: false } }.
  return { title: "Catalogue" };
}

export default async function CataloguePage({ searchParams }) {
  // TODO 4: searchParams is a Promise. No page means page 1; "1" itself redirects to "/"; anything that isn't a
  // whole number from 1 to the last page (like "0", "4", "abc" or two page values) calls notFound().
  const page = Number(searchParams.page ?? 1);
  const items = await listItems();
  const pageCount = Math.ceil(items.length / PAGE_SIZE);
  // TODO 5: page 1 should show the first two items. Previous only after page 1 (page 2's Previous goes to "/"),
  // and Next only before the last page.
  const shown = items.slice(page * PAGE_SIZE, page * PAGE_SIZE + PAGE_SIZE);
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
        <Link href={`/?page=${page - 1}`}>Previous</Link>
        <p>
          Page {page} of {pageCount}
        </p>
        {page <= pageCount && <Link href={`/?page=${page + 1}`}>Next</Link>}
      </nav>
    </main>
  );
}
