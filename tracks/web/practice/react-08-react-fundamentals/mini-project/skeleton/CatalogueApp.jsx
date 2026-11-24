import { useState } from "react";
import { COLOURS } from "./products.js";
import { ProductCard } from "./ProductCard.jsx";

export function CatalogueApp({ products }) {
  // TODO 2: Keep three things in state: the search text (""), the chosen colour ("All") and the cart count (0).

  // TODO 3: Work out the visible products on every render. A product is visible when its name contains the
  //         search text (ignoring capitals and spaces at the ends) and its colour is the chosen one, or the choice is "All".
  const visible = products;

  return (
    <main className="catalogue">
      <header className="top">
        <h1>Dhaaga & Co.</h1>
        {/* TODO 4: Show the real cart count, like Cart (2). */}
        <p className="cart-count">Cart (0)</p>
      </header>

      {/* TODO 5: Make the search box controlled by the search text. */}
      <label htmlFor="search">Search</label>
      <input id="search" type="search" />

      {/* TODO 6: Inside this div, one button for "All" and then one per colour in COLOURS. The chosen one has
          aria-pressed true and className "chip chip--selected"; the others aria-pressed false and "chip".
          Tapping a button chooses its colour. */}
      <div className="colour-filter" role="group" aria-label="Colour"></div>

      <p className="showing">
        Showing {visible.length} of {products.length} items
      </p>

      {/* TODO 7: One ProductCard per visible product, keyed by its id; its Add adds one to the cart count.
          When nothing is visible, show <p className="empty">No items match your search.</p> instead. */}
      <section className="grid"></section>
    </main>
  );
}
