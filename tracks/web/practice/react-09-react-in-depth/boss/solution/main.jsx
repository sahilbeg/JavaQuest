import { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import { SaleStock } from "./SaleStock.jsx";

// A pretend stock API for the preview. Golu (DC-1042) answers slowly, like on 3G.
const STOCK = { "DC-1042": 7, "DC-2001": 3, "DC-3005": 0 };
let requests = 0;
window.fetch = async (url) => {
  requests += 1;
  const id = url.split("/").pop();
  await new Promise((resolve) => setTimeout(resolve, id === "DC-1042" ? 1500 : 300));
  return { ok: true, status: 200, json: async () => ({ id, left: STOCK[id] }) };
};

/** Shows how many requests the API has had, checking twice a second. */
function RequestCounter() {
  const [, setTick] = useState(0);
  useEffect(() => {
    const id = setInterval(() => setTick((t) => t + 1), 500);
    return () => clearInterval(id);
  }, []);
  return <p>Requests the stock API has had: {requests}</p>;
}

function App() {
  const [itemId, setItemId] = useState("DC-1042");
  return (
    <main>
      <h1>Diwali sale</h1>
      {["DC-1042", "DC-2001", "DC-3005"].map((id) => (
        <button key={id} onClick={() => setItemId(id)}>
          {id}
        </button>
      ))}
      <SaleStock itemId={itemId} />
      <RequestCounter />
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
