// Starts the app for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { Cart } from "./Cart.jsx";
import "./style.css";

const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 },
  { id: "DC-1046", name: "Baby booties", paise: 35000, qty: 2 },
  { id: "DC-1044", name: "Mint keychain", paise: 12000, qty: 3 },
  { id: "DC-1043", name: "Coaster set", paise: 15000, qty: 1 },
  { id: "DC-1047", name: "Coaster set", paise: 15000, qty: 4 },
];

createRoot(document.getElementById("root")).render(
  <main>
    <h1>Your cart</h1>
    <Cart initialItems={ITEMS} />
  </main>,
);
