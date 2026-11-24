import { createRoot } from "react-dom/client";
import { SearchBar } from "./SearchBar.jsx";
import { ProductCard } from "./ProductCard.jsx";
import { NotifyButton } from "./NotifyButton.jsx";

const GOLU = { name: "Golu the elephant", paise: 45000 };
const TOTE = { name: "Indigo tote bag", paise: 89900 };

createRoot(document.getElementById("root")).render(
  <main>
    <SearchBar />
    <h2>Wide shelf</h2>
    <div style={{ width: "600px" }}>
      <ProductCard item={GOLU} />
    </div>
    <h2>Narrow cart drawer</h2>
    <div style={{ width: "300px" }}>
      <ProductCard item={GOLU} />
    </div>
    <h2>{TOTE.name}: sold out</h2>
    <NotifyButton />
  </main>,
);
