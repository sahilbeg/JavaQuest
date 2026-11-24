import { useState } from "react";
import { createRoot } from "react-dom/client";
import { AddressForm } from "./AddressForm.jsx";

function App() {
  const [saved, setSaved] = useState(null);
  return (
    <main>
      <h1>Where should we send it?</h1>
      <AddressForm onSubmit={setSaved} />
      {saved && (
        <p>
          Saved: {saved.name}, {saved.phone}, {saved.line1}, {saved.pincode}
        </p>
      )}
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
