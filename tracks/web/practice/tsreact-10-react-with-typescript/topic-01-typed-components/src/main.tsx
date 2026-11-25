// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { ItemCard } from "./ItemCard.tsx";
import { StoreHeader } from "./StoreHeader.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <main>
      <StoreHeader />
      <ItemCard name="Golu the elephant" paise={45000} colour="Indigo" />
      <ItemCard name="Marigold coaster set" paise={15000} colour="Marigold" />
    </main>,
  );
}
