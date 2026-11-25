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
      {/* TODO 6: wrap only SimilarItems in <ErrorBoundary resetKey={itemId} fallback={(error, reset) => ...}>.
          The fallback is <div role="alert"> with <p>Similar items are taking a break.</p> and a Try again button that calls reset. */}
      <SimilarItems items={similar} />
    </article>
  );
}
