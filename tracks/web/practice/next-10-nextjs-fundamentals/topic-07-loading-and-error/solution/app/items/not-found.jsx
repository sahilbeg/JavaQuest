import Link from "next/link";

/** Shown when a page in the shop calls notFound(). */
export default function ItemNotFound() {
  return (
    <>
      <h1>We couldn't find that piece.</h1>
      <Link href="/items">Back to the shop</Link>
    </>
  );
}
