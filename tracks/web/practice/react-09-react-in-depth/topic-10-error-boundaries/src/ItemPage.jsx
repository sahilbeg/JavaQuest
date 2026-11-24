import { ErrorBoundary } from "./ErrorBoundary.jsx";
import { Reviews } from "./Reviews.jsx";
import { rupees } from "./money.js";

/** Golu the elephant's page: what you need to buy him, then the reviews. */
export function ItemPage({ reviews }) {
  return (
    <main>
      <h1>Golu the elephant</h1>
      <p>{rupees(45000)}</p>
      <button>Add to cart</button>
      {/* TODO 5: wrap Reviews in <ErrorBoundary fallback="Reviews are taking a break. You can still buy Golu."> */}
      <Reviews reviews={reviews} />
    </main>
  );
}
