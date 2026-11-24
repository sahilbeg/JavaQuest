// Starts the app for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { ProductList, GiftsUnder } from "./Catalogue.jsx";
import "./style.css";

const PRODUCTS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1043", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1044", name: "Mint keychain", paise: 12000 },
  { id: "DC-1045", name: "Indigo tote bag", paise: 89900 },
  { id: "DC-1046", name: "Rani pink baby booties", paise: 35000 },
  { id: "DC-1047", name: "Off-white coaster set", paise: 15000 },
];

createRoot(document.getElementById("root")).render(
  <main>
    <section>
      <h2>Everything</h2>
      <ProductList products={PRODUCTS} />
    </section>
    <GiftsUnder products={PRODUCTS} maxPaise={50000} />
  </main>,
);
