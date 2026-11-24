// Starts the app for the preview. The tests never import this file.
import { useState } from "react";
import { createRoot } from "react-dom/client";
import { ColourChip, PriceTag } from "./ColourChip.jsx";
import { HEX } from "./colours.js";
import "./style.css";

function Demo() {
  const [colour, setColour] = useState("Mint");
  return (
    <main>
      <h1>Baby booties</h1>
      <div className="chips">
        {Object.keys(HEX).map((name) => (
          <ColourChip key={name} colour={name} selected={name === colour} onSelect={setColour} />
        ))}
      </div>
      <p>You picked: {colour}</p>
      <PriceTag paise={35000} />
      <PriceTag paise={31500} onSale />
    </main>
  );
}

createRoot(document.getElementById("root")).render(<Demo />);
