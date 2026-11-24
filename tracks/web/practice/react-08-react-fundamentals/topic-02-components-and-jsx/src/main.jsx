// Starts the app for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { StoreFront } from "./StoreFront.jsx";
import "./style.css";

createRoot(document.getElementById("root")).render(<StoreFront />);
