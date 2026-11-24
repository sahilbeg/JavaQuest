// Starts the app for the preview. The tests never import this file.
import { useState } from "react";
import { createRoot } from "react-dom/client";
import { OrderForm } from "./OrderForm.jsx";
import "./style.css";

function Demo() {
  const [lastOrder, setLastOrder] = useState(null);
  return (
    <main>
      <h1>Order from Dhaaga & Co.</h1>
      <OrderForm onSubmit={setLastOrder} />
      {lastOrder !== null && <pre className="sent">{JSON.stringify(lastOrder, null, 2)}</pre>}
    </main>
  );
}

createRoot(document.getElementById("root")).render(<Demo />);
