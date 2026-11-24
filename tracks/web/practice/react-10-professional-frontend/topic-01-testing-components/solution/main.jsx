import { useState } from "react";
import { createRoot } from "react-dom/client";
import { ProductCard } from "./ProductCard.jsx";
import "./style.css";

const PRODUCTS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 7 },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000, stock: 2 },
  { id: "DC-1301", name: "Rani pink tote bag", paise: 89900, stock: 0 },
];

function Shelf() {
  const [message, setMessage] = useState("Nothing added yet.");
  return (
    <main>
      <h1>dhaaga.in</h1>
      <div className="shelf">
        {PRODUCTS.map((product) => (
          <ProductCard key={product.id} product={product} onAdd={(id, quantity) => setMessage(`Added ${quantity} × ${id}`)} />
        ))}
      </div>
      <p role="status">{message}</p>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<Shelf />);
