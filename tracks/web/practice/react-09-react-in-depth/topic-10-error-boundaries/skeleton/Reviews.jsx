/**
 * Customer reviews. Each review is { id, author, stars, text }.
 * It has a bug on purpose: a review whose text is null crashes it. Leave it: the boundary is the fix.
 */
export function Reviews({ reviews }) {
  return (
    <section>
      <h2>Reviews</h2>
      <ul>
        {reviews.map((review) => (
          <li key={review.id}>
            {"★".repeat(review.stars)} {review.text.trim()} ({review.author})
          </li>
        ))}
      </ul>
    </section>
  );
}
