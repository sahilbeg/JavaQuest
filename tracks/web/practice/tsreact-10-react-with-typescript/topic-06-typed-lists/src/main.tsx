// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { SHELF } from "./yarn.ts";
import { YarnShelf } from "./YarnShelf.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <main>
      <h1>Aundh shop: yarn shelf</h1>
      <YarnShelf yarns={SHELF} />
    </main>,
  );
}
