import Link from "next/link";

/** Shown when an item's page calls notFound(). It's finished. */
export default function ItemNotFound() {
  return (
    <>
      <h1>We couldn't find that piece.</h1>
      <Link href="/items">Back to the shop</Link>
    </>
  );
}
