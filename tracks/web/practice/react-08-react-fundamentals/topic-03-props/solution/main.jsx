// Starts the app for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { FeaturedProducts } from "./ProductCard.jsx";
import "./style.css";

createRoot(document.getElementById("root")).render(<FeaturedProducts />);
