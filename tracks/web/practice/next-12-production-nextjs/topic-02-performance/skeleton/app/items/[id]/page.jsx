import { notFound } from "next/navigation";
import { listItems, getItem, getReviews, getStock } from "../../../lib/shop.js";
import { formatRupees } from "../../../lib/money.js";
import { AddToCartButton } from "../../../components/AddToCartButton.jsx";

// Tells Next.js which item pages to build ahead of time, during next build.
export async function generateStaticParams() {
  // TODO 1: return one { id } object per item from listItems(), in catalogue order, like [{ id: "DC-1042" }, ...].
  return [];
}

export default async function ItemPage({ params }) {
  const { id } = await params;
  // TODO 2: these three wait for each other: 600 ms, then 600 ms, then 600 ms. Start all three at once with Promise.all.
  const item = await getItem(id);
  const reviews = await getReviews(id);
  const stock = await getStock(id);
  if (!item) {
    notFound();
  }
  return (
    <main>
      <h1>{item.name}</h1>
      <p>{formatRupees(item.paise)}</p>
      <p>{item.description}</p>
      {/* TODO 3: everything passed to a client component is sent to the phone. Pass only id and stock. */}
      <AddToCartButton id={item.id} item={item} reviews={reviews} stock={stock} />
      <section aria-labelledby="reviews-heading">
        <h2 id="reviews-heading">Reviews ({reviews.length})</h2>
        <ul>
          {reviews.map((review) => (
            <li key={review.by}>
              {review.stars}/5 from {review.by}: {review.text}
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}
