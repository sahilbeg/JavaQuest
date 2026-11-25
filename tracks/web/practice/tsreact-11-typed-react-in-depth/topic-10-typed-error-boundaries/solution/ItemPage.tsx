import { ErrorBoundary } from "./ErrorBoundary.tsx";
import { SimilarItems } from "./SimilarItems.tsx";
import type { SimilarItem } from "./SimilarItems.tsx";

export interface ItemPageProps {
  itemId: string;
  name: string;
  price: string;
  similar: SimilarItem[];
  onAdd: () => void;
}

/** An item's page: heading, price, Add to cart, and some suggestions. */
export function ItemPage({ itemId, name, price, similar, onAdd }: ItemPageProps) {
  return (
    <article>
      <h1>{name}</h1>
      <p>{price}</p>
      <button onClick={onAdd}>Add to cart</button>
      <ErrorBoundary
        resetKey={itemId}
        fallback={(_error, reset) => (
          <div role="alert">
            <p>Similar items are taking a break.</p>
            <button onClick={reset}>Try again</button>
          </div>
        )}
      >
        <SimilarItems items={similar} />
      </ErrorBoundary>
    </article>
  );
}
