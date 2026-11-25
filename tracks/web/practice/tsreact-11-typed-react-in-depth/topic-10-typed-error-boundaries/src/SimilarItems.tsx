export interface SimilarItem {
  id: string;
  name: string;
}

/** "You may also like": a list of other items. It trusts every item to have a name. Don't fix it. */
export function SimilarItems({ items }: { items: SimilarItem[] }) {
  return (
    <section>
      <h2>You may also like</h2>
      <ul>
        {items.map((item) => (
          <li key={item.id}>{item.name.trim()}</li>
        ))}
      </ul>
    </section>
  );
}
