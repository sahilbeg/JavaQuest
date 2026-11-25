// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { CustomOrderForm } from "./CustomOrderForm.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <main>
      <h1>Ask for a custom order</h1>
      <CustomOrderForm onSend={(request) => console.log("Request sent:", JSON.stringify(request))} />
    </main>,
  );
}
