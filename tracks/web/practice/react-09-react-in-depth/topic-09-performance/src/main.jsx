import { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import { Catalogue } from "./Catalogue.jsx";
import { ITEMS } from "./items.js";
import { rowRenders } from "./ProductRow.jsx";
import { sortStats } from "./sortByPrice.js";

/** Shows how much work the catalogue has done, checking twice a second. */
function WorkCounter() {
  const [, setTick] = useState(0);
  useEffect(() => {
    const id = setInterval(() => setTick((t) => t + 1), 500);
    return () => clearInterval(id);
  }, []);
  return (
    <p>
      Rows rendered: {rowRenders.length}. Sorts run: {sortStats.runs}.
    </p>
  );
}

createRoot(document.getElementById("root")).render(
  <main>
    <h1>Dhaaga &amp; Co.</h1>
    <Catalogue items={ITEMS} />
    <WorkCounter />
  </main>,
);
