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
  // TODO 4: Typed state: query (""), colour (Colour | "All", starting "All"), cart (Record<string, number>, starting {}).
  //         add(id) makes a new cart with that id's count raised by 1: (cart[id] ?? 0) + 1.
  // TODO 5: The cart count is every number in the cart added up: Object.values(cart).
  // TODO 6: The search box, and one button per entry in FILTERS with aria-pressed.
  // TODO 7: visible = the products matching the search and the colour.
  //         Show p.showing, then section.grid with a ProductCard per visible product (key, inCart, onAdd),
  //         or p.empty when nothing matches.
  return (
    <div className="catalogue">
      <header>
        <h1>Dhaaga & Co.</h1>
        <p className="cart-count">Cart (0)</p>
      </header>
    </div>
  );
}
