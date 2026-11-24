// Starts the app for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { CatalogueApp } from "./CatalogueApp.jsx";
import { PRODUCTS } from "./products.js";
import "./style.css";

createRoot(document.getElementById("root")).render(<CatalogueApp products={PRODUCTS} />);
