import { useState } from "react";
import { LOW_GRAMS } from "./yarn.ts";
import type { Yarn } from "./yarn.ts";

export type SortBy = "shelf" | "grams";

export interface YarnShelfProps {
  yarns: readonly Yarn[];
}

export function YarnShelf({ yarns }: YarnShelfProps) {
  const [sortBy, setSortBy] = useState<SortBy>("shelf");

  // TODO 1: For "grams", a sorted COPY with the fewest grams first: [...yarns].sort((a, b) => a.grams - b.grams).
  //         For "shelf", the yarns as they are. (yarns is readonly, so yarns.sort(...) is a type error.)
  const shown: readonly Yarn[] = yarns;

  // TODO 2: Add up the grams of every yarn with reduce.
  const total: number = 0;

  return (
    <section className="yarn-shelf">
      <div className="sort">
        <button type="button" aria-pressed={sortBy === "shelf"} onClick={() => setSortBy("shelf")}>
          Shelf order
        </button>
        <button type="button" aria-pressed={sortBy === "grams"} onClick={() => setSortBy("grams")}>
          Fewest grams first
        </button>
      </div>
      <ul className="yarn-list">
        {/* TODO 3: One <li> per yarn in shown, keyed by yarn.id, reading "Marigold: 120 g". */}
        {/* TODO 4: Give a row className="low" when it has fewer than LOW_GRAMS grams, and no class otherwise. */}
      </ul>
      <p className="total">Total: {total} g</p>
    </section>
  );
}
