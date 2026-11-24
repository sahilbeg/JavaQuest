// Starts the app for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { ItemCard } from "./ItemCard.jsx";
import "./style.css";

const log = (text) => () => console.log(text);

createRoot(document.getElementById("root")).render(
  <main>
    <ItemCard name="Indigo tote bag" paise={89900} stock={7} giftWrap onAdd={log("add BAG")} onNotify={log("notify BAG")} />
    <ItemCard name="Mint keychain" paise={12000} stock={1} onAdd={log("add KEY")} onNotify={log("notify KEY")} />
    <ItemCard name="Golu the elephant" paise={45000} stock={0} onAdd={log("add AMI")} onNotify={log("notify AMI")} />
  </main>,
);
