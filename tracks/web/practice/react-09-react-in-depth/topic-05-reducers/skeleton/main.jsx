import { createRoot } from "react-dom/client";
import { CartPanel } from "./CartPanel.jsx";

const ITEMS = [
  { code: "CST", name: "Coaster set", paise: 15000 },
  { code: "KEY", name: "Keychain", paise: 12000 },
  { code: "BTY", name: "Baby booties", paise: 35000 },
];

createRoot(document.getElementById("root")).render(
  <main>
    <h1>Dhaaga &amp; Co.</h1>
    <CartPanel items={ITEMS} />
  </main>,
);
