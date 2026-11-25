// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { ITEMS } from "./items.ts";
import { ShopPage } from "./ShopPage.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(<ShopPage items={ITEMS} />);
}
