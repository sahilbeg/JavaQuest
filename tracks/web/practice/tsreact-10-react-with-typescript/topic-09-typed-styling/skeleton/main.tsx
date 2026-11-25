// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { Button } from "./Button.tsx";
import { ColourSwatch } from "./ColourSwatch.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <main>
      <h1>Sana's swatch book</h1>
      <p>
        <ColourSwatch colour="Marigold" /> <ColourSwatch colour="Indigo" /> <ColourSwatch colour="Rani pink" />{" "}
        <ColourSwatch colour="Mint" /> <ColourSwatch colour="Off-white" size={40} />
      </p>
      <p>
        <Button onClick={() => console.log("Paid")}>Pay by UPI</Button>{" "}
        <Button variant="secondary">Keep shopping</Button>{" "}
        <Button variant="danger" size="sm">
          Remove
        </Button>{" "}
        <Button disabled>Sold out</Button>
      </p>
    </main>,
  );
}
