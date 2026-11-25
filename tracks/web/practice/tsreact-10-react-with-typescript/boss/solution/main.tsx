// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { Cart } from "./Cart.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <Cart
      initialLines={[
        { id: "DC-1031", qty: 2 },
        { id: "DC-1042", qty: 1 },
        { id: "toString", qty: 1 },
      ]}
    />,
  );
}
