// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { BuyBox } from "./BuyBox.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <main>
      <BuyBox name="Golu the elephant" stock={{ kind: "sold-out", backOn: "14 Dec" }} onAdd={() => console.log("Golu added")} />
      <BuyBox name="Marigold coaster set" stock={{ kind: "in-stock", count: 12 }} onAdd={() => console.log("Coasters added")} />
      <BuyBox name="Indigo tote bag" stock={{ kind: "in-stock", count: 2 }} onAdd={() => console.log("Tote bag added")} />
      <BuyBox name="Rani pink baby booties" stock={{ kind: "made-to-order", days: 5 }} onAdd={() => console.log("Booties added")} />
    </main>,
  );
}
