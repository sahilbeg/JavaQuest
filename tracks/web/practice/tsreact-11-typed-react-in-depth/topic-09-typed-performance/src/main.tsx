import { useState } from "react";
import { createRoot } from "react-dom/client";
import { OrderHistory } from "./OrderHistory.tsx";
import { rowRenders } from "./OrderRow.tsx";
import { ORDERS, filterStats } from "./orders.ts";

function App() {
  const [counters, setCounters] = useState<string>("");
  return (
    <main>
      <h1>Order history</h1>
      <OrderHistory orders={ORDERS} />
      <button onClick={() => setCounters(`${rowRenders.length} row renders, ${filterStats.runs} searches`)}>Show the counters</button>
      <p>{counters}</p>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
