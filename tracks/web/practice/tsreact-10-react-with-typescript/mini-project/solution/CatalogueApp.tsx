import { useState } from "react";
import { ProductCard } from "./ProductCard.tsx";
import { COLOURS } from "./products.ts";
import type { Colour, Product } from "./products.ts";

export interface CatalogueAppProps {
  products: readonly Product[];
}

/** The colour buttons, in order: All first, then every colour. */
export const FILTERS: readonly (Colour | "All")[] = ["All", ...COLOURS];

export function CatalogueApp({ products }: CatalogueAppProps) {
  const [query, setQuery] = useState("");
  const [colour, setColour] = useState<Colour | "All">("All");
  const [cart, setCart] = useState<Record<string, number>>({});

  function add(id: string) {
    setCart({ ...cart, [id]: (cart[id] ?? 0) + 1 });
  }

  const count = Object.values(cart).reduce((sum, n) => sum + n, 0);
  const text = query.trim().toLowerCase();
  const visible = products.filter(
    (p) => p.name.toLowerCase().includes(text) && (colour === "All" || p.colour === colour),
  );

  return (
    <div className="catalogue">
      <header>
        <h1>Dhaaga & Co.</h1>
        <p className="cart-count">Cart ({count})</p>
      </header>
      <label htmlFor="search">Search</label>
      <input id="search" type="search" value={query} onChange={(e) => setQuery(e.target.value)} />
      <div className="colour-filter">
        {FILTERS.map((c) => (
          <button key={c} type="button" aria-pressed={colour === c} onClick={() => setColour(c)}>
            {c}
          </button>
        ))}
      </div>
      <p className="showing">
        Showing {visible.length} of {products.length} items
      </p>
      {visible.length === 0 ? (
        <p className="empty">No items match your search.</p>
      ) : (
        <section className="grid">
          {visible.map((p) => (
            <ProductCard key={p.id} product={p} inCart={cart[p.id] ?? 0} onAdd={add} />
          ))}
        </section>
      )}
    </div>
  );
}
