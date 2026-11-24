import { safeUrl } from "./safeUrl.js";

/** Customer reviews on a product page. Everything in a review was typed by a stranger. */
export function ReviewList({ reviews }) {
  return (
    <ul className="reviews">
      {reviews.map((review) => {
        const link = safeUrl(review.website);
        return (
          <li key={review.id}>
            <p className="stars">
              <span aria-hidden="true">{"★".repeat(review.rating) + "☆".repeat(5 - review.rating)}</span> {review.rating} out of 5
            </p>
            <p className="text">{review.text}</p>
            <p className="author">
              {link ? (
                <a href={link} target="_blank" rel="nofollow noopener noreferrer">
                  {review.author}
                </a>
              ) : (
                review.author
              )}
            </p>
          </li>
        );
      })}
    </ul>
  );
}
