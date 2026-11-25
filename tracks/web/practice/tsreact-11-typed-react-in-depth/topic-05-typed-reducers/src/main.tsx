import { createRoot } from "react-dom/client";
import { OrderBoard } from "./OrderBoard.tsx";
import type { Order } from "./orders.ts";

const TODAY: Order[] = [
  { id: "order-17", customer: "Mrs. Kulkarni", status: "new" },
  { id: "order-18", customer: "Asha Rao", status: "packed" },
  { id: "order-19", customer: "Vikram Nair", status: "shipped" },
];

createRoot(document.getElementById("root")!).render(<OrderBoard start={TODAY} />);
