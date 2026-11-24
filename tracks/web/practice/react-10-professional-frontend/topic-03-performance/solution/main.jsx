import { createRoot } from "react-dom/client";
import { ProductGrid } from "./ProductGrid.jsx";
import "./style.css";

const PRODUCTS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1102", name: "Indigo coaster set", paise: 15000 },
  { id: "DC-1201", name: "Mint keychain", paise: 12000 },
  { id: "DC-1301", name: "Rani pink tote bag", paise: 89900 },
  { id: "DC-1401", name: "Off-white baby booties", paise: 35000 },
  { id: "DC-1043", name: "Chotu the tiger", paise: 45000 },
  { id: "DC-1202", name: "Marigold keychain", paise: 12000 },
];

createRoot(document.getElementById("root")).render(
  <main>
    <h1>Diwali catalogue</h1>
    <p>The photos aren't on this preview server, so you'll see their alt text in boxes of the right size.</p>
    <ProductGrid products={PRODUCTS} />
  </main>
);
