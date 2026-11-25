import { notFound } from "next/navigation";
import { listItems, getItem, getReviews, getStock } from "../../../lib/shop.js";
import { formatRupees } from "../../../lib/money.js";
import { AddToCartButton } from "../../../components/AddToCartButton.jsx";

// Tells Next.js which item pages to build ahead of time, during next build.
export async function generateStaticParams() {
  const items = await listItems();
  return items.map((item) => ({ id: item.id }));
}

export default async function ItemPage({ params }) {
  const { id } = await params;
  const [item, reviews, stock] = await Promise.all([getItem(id), getReviews(id), getStock(id)]);
  if (!item) {
    notFound();
  }
  return (
    <main>
      <h1>{item.name}</h1>
      <p>{formatRupees(item.paise)}</p>
      <p>{item.description}</p>
      <AddToCartButton id={item.id} stock={stock} />
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
