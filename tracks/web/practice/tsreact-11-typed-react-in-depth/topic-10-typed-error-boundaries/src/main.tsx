import { useState } from "react";
import { createRoot } from "react-dom/client";
import { ItemPage } from "./ItemPage.tsx";
import type { SimilarItem } from "./SimilarItems.tsx";

// Golu's suggestions came back with one broken item: it has no name. (The cast pretends the server's data is fine.)
const GOLU_SIMILAR = [{ id: "DC-2001", name: "Marigold coaster set" }, { id: "DC-3005" }] as SimilarItem[];
const COASTER_SIMILAR: SimilarItem[] = [{ id: "DC-1042", name: "Golu the elephant" }];

function App() {
  const [itemId, setItemId] = useState<string>("DC-1042");
  const [inCart, setInCart] = useState<number>(0);
  const golu = itemId === "DC-1042";
  return (
    <main>
      <p>Cart: {inCart}</p>
      <button onClick={() => setItemId(golu ? "DC-2001" : "DC-1042")}>{golu ? "Show the coaster set" : "Show Golu"}</button>
      <ItemPage
        itemId={itemId}
        name={golu ? "Golu the elephant" : "Marigold coaster set"}
        price={golu ? "₹450.00" : "₹150.00"}
        similar={golu ? GOLU_SIMILAR : COASTER_SIMILAR}
        onAdd={() => setInCart(inCart + 1)}
      />
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
