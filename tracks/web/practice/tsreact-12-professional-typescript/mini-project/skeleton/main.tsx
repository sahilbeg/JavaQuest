import { createRoot } from "react-dom/client";
import { App } from "./App.tsx";

// The page and its stylesheet are in index.html. The preview builds this file into main.js.
const root = document.getElementById("root");
if (root !== null) {
  createRoot(root).render(<App />);
}
