import { useState } from "react";
import { LOW_GRAMS } from "./yarn.ts";
import type { Yarn } from "./yarn.ts";

export type SortBy = "shelf" | "grams";

export interface YarnShelfProps {
  yarns: readonly Yarn[];
}

export function YarnShelf({ yarns }: YarnShelfProps) {
  const [sortBy, setSortBy] = useState<SortBy>("shelf");

  const shown: readonly Yarn[] = sortBy === "grams" ? [...yarns].sort((a, b) => a.grams - b.grams) : yarns;
  const total = yarns.reduce((sum, yarn) => sum + yarn.grams, 0);

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
        {shown.map((yarn) => (
          <li key={yarn.id} className={yarn.grams < LOW_GRAMS ? "low" : undefined}>
            {yarn.colour}: {yarn.grams} g
          </li>
        ))}
      </ul>
      <p className="total">Total: {total} g</p>
    </section>
  );
}
