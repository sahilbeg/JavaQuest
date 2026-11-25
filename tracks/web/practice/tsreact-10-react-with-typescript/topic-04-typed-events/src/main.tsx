// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { CounterPad } from "./CounterPad.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <main>
      <h1>FC Road stall: counter</h1>
      <CounterPad onAdd={(code) => console.log("Added to the bill:", code)} />
    </main>,
  );
}
