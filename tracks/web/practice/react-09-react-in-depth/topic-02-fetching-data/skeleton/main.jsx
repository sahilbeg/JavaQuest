import { useState } from "react";
import { createRoot } from "react-dom/client";
import { OrderStatus } from "./OrderStatus.jsx";

// A pretend API for the preview: order-17 answers slowly, order-18 quickly, order-99 doesn't exist.
const ORDERS = {
  "order-17": { id: "order-17", status: "PACKED" },
  "order-18": { id: "order-18", status: "SHIPPED" },
};
window.fetch = async (url) => {
  const id = url.split("/").pop();
  await new Promise((resolve) => setTimeout(resolve, id === "order-17" ? 2000 : 400));
  const order = ORDERS[id];
  return order
    ? { ok: true, status: 200, json: async () => order }
    : { ok: false, status: 404, json: async () => ({ message: "not found" }) };
};

function App() {
  const [orderId, setOrderId] = useState("order-17");
  return (
    <main>
      <h1>Where is my order?</h1>
      {["order-17", "order-18", "order-99"].map((id) => (
        <button key={id} onClick={() => setOrderId(id)}>
          {id}
        </button>
      ))}
      <OrderStatus orderId={orderId} />
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
