import { safeUrl } from "./safeUrl.js";

/** Customer reviews on a product page. Everything in a review was typed by a stranger. */
export function ReviewList({ reviews }) {
  return (
    <ul className="reviews">
      {reviews.map((review) => (
        <li key={review.id}>
          <p className="stars">
            <span aria-hidden="true">{"★".repeat(review.rating) + "☆".repeat(5 - review.rating)}</span> {review.rating} out of 5
          </p>
          {/* TODO 2: show the review's words as plain text with {review.text}. Never turn a customer's words into HTML. */}
          <p className="text" dangerouslySetInnerHTML={{ __html: review.text }} />
          {/* TODO 3: show the author as plain text too. Link it only when safeUrl(review.website) gives an address, with target="_blank" and rel="nofollow noopener noreferrer". */}
          <p className="author">
            <a href={review.website} dangerouslySetInnerHTML={{ __html: review.author }} />
          </p>
        </li>
      ))}
    </ul>
  );
}
