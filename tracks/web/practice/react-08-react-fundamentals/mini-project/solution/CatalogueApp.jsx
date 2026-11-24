import { useState } from "react";
import { COLOURS } from "./products.js";
import { ProductCard } from "./ProductCard.jsx";

export function CatalogueApp({ products }) {
  const [query, setQuery] = useState("");
  const [colour, setColour] = useState("All");
  const [cartCount, setCartCount] = useState(0);

  const text = query.trim().toLowerCase();
  const visible = products.filter(
    (p) => p.name.toLowerCase().includes(text) && (colour === "All" || p.colour === colour),
  );

  return (
    <main className="catalogue">
      <header className="top">
        <h1>Dhaaga & Co.</h1>
        <p className="cart-count">Cart ({cartCount})</p>
      </header>

      <label htmlFor="search">Search</label>
      <input id="search" type="search" value={query} onChange={(event) => setQuery(event.target.value)} />

      <div className="colour-filter" role="group" aria-label="Colour">
        {["All", ...COLOURS].map((c) => (
          <button
            key={c}
            type="button"
            className={colour === c ? "chip chip--selected" : "chip"}
            aria-pressed={colour === c}
            onClick={() => setColour(c)}
          >
            {c}
          </button>
        ))}
      </div>

      <p className="showing">
        Showing {visible.length} of {products.length} items
      </p>

      <section className="grid">
        {visible.length === 0 ? (
          <p className="empty">No items match your search.</p>
        ) : (
          visible.map((p) => <ProductCard key={p.id} product={p} onAdd={() => setCartCount((n) => n + 1)} />)
        )}
      </section>
    </main>
  );
}
