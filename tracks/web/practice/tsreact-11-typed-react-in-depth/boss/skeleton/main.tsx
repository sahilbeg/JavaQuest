import { createRoot } from "react-dom/client";
import { Checkout } from "./Checkout.tsx";

createRoot(document.getElementById("root")!).render(
  <main>
    <h1>Checkout: Golu the elephant</h1>
    <Checkout itemsPaise={45000} />
  </main>,
);
