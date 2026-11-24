// Starts the app for the preview. The tests never import this file.
import { useState } from "react";
import { createRoot } from "react-dom/client";
import { ProductTile, SearchBar } from "./ProductTile.jsx";
import "./style.css";

function Demo() {
  const [log, setLog] = useState("Nothing yet");
  return (
    <main>
      <SearchBar onSearch={(text) => setLog(`Searched for "${text}"`)} />
      <ProductTile
        id="DC-1042"
        name="Golu the elephant"
        paise={45000}
        onOpen={(id) => setLog(`Opened the page for ${id}`)}
        onAdd={(id) => setLog(`Added ${id} to the cart`)}
      />
      <p className="log">{log}</p>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<Demo />);
