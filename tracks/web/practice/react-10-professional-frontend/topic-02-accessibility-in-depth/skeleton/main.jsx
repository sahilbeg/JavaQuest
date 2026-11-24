import { useState } from "react";
import { createRoot } from "react-dom/client";
import { Disclosure } from "./Disclosure.jsx";
import { RemoveItemButton } from "./RemoveItemButton.jsx";
import "./style.css";

function CartPage() {
  const [items, setItems] = useState(["Golu the elephant", "Marigold coaster set"]);
  return (
    <main>
      <h1>Your cart</h1>
      <ul className="lines">
        {items.map((name) => (
          <li key={name}>
            {name} <RemoveItemButton itemName={name} onRemove={() => setItems(items.filter((item) => item !== name))} />
          </li>
        ))}
      </ul>
      {items.length === 0 && <p>Your cart is empty.</p>}

      <h2>Questions</h2>
      <Disclosure title="Delivery and returns">
        <p>Delivery inside Pune costs ₹60. It's free above ₹999.</p>
        <p>Returns within 7 days, unused and with the tag.</p>
      </Disclosure>
      <Disclosure title="Can I get a name stitched on?">
        <p>Yes. A name stitched on costs ₹50.</p>
      </Disclosure>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<CartPage />);
