import { getItems } from "../lib/catalogue.js";
import { rupees } from "../lib/money.js";
import { AddToCartButton } from "./AddToCartButton.jsx";

/** The home page, drawn on the server. */
export default function HomePage() {
  // TODO 1: make this an async server component and await getItems().
  return (
    <>
      <h1>Handmade in Pune</h1>
      {/* TODO 2: a <ul> with one <li> per item: <h2> name, <p> rupees(item.paise), and an AddToCartButton. */}
    </>
  );
}
