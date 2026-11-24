import { createRoot } from "react-dom/client";
import { groupOrdersByDay } from "./groupOrdersByDay.js";
import { Tabs } from "./Tabs.jsx";
import "./style.css";

const ORDERS = [
  { id: "order-17", placedAt: "2026-11-08T10:15:00+05:30", paise: 45000 },
  { id: "order-18", placedAt: "2026-11-08T18:40:00+05:30", paise: 15000 },
  { id: "order-19", placedAt: "2026-11-07T12:00:00+05:30", paise: 89900 },
  { id: "order-20", placedAt: "2026-11-09T02:10:00+05:30", paise: 12000 },
];

const TABS = [
  { id: "description", label: "Description", content: "Golu is crocheted in Indigo cotton, with Marigold ears." },
  { id: "care", label: "Care", content: "Hand wash in cold water. Dry flat, away from the sun." },
  { id: "delivery", label: "Delivery", content: "₹60 inside Pune, free above ₹999." },
];

createRoot(document.getElementById("root")).render(
  <main>
    <h1>The mock loop</h1>
    <h2>Round 1: orders by day</h2>
    <ul>
      {groupOrdersByDay(ORDERS).map((group) => (
        <li key={group.day}>
          {group.day}: {group.count} orders, {group.totalPaise} paise
        </li>
      ))}
    </ul>
    <h2>Round 2: tabs</h2>
    <p>Click a tab, then try Tab, the arrow keys, Home and End.</p>
    <Tabs label="About Golu" tabs={TABS} />
    <h2>Round 3: useFetch</h2>
    <p>This round has no preview: the checks give it a pretend server.</p>
  </main>
);
