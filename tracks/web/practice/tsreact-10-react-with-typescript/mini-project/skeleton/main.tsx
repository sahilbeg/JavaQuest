// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { CatalogueApp } from "./CatalogueApp.tsx";
import { PRODUCTS } from "./products.ts";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(<CatalogueApp products={PRODUCTS} />);
}
