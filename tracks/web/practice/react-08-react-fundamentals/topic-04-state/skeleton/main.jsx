// Starts the app for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { QuantityPicker } from "./QuantityPicker.jsx";
import "./style.css";

createRoot(document.getElementById("root")).render(
  <main>
    <QuantityPicker name="Golu the elephant" paise={45000} stock={3} />
    <QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />
  </main>,
);
