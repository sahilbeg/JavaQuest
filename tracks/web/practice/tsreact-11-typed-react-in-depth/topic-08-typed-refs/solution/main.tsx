import { useState } from "react";
import { createRoot } from "react-dom/client";
import { PincodeBoxes } from "./PincodeBoxes.tsx";

function App() {
  const [pincode, setPincode] = useState<string | null>(null);
  return (
    <main>
      <h1>Where should we deliver?</h1>
      <PincodeBoxes onComplete={setPincode} />
      <p>{pincode ? `Delivering to PIN code ${pincode}` : "Type your 6-digit PIN code"}</p>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
