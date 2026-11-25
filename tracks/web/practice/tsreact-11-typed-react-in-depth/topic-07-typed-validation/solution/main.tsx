import { useState } from "react";
import { createRoot } from "react-dom/client";
import { CustomOrderForm } from "./CustomOrderForm.tsx";
import type { CustomRequest } from "./request.ts";

function App() {
  const [sent, setSent] = useState<CustomRequest | null>(null);
  return (
    <main>
      <h1>Ask for a custom amigurumi</h1>
      <CustomOrderForm onSubmit={setSent} />
      {sent && <pre>{JSON.stringify(sent, null, 2)}</pre>}
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
