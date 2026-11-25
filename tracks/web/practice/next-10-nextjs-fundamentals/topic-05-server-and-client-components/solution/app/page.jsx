import { getItems } from "../lib/catalogue.js";
import { rupees } from "../lib/money.js";
import { AddToCartButton } from "./AddToCartButton.jsx";

/** The home page, drawn on the server. */
export default async function HomePage() {
  const items = await getItems();
  return (
    <>
      <h1>Handmade in Pune</h1>
      <ul>
        {items.map((item) => (
          <li key={item.id}>
            <h2>{item.name}</h2>
            <p>{rupees(item.paise)}</p>
            <AddToCartButton itemId={item.id} name={item.name} />
          </li>
        ))}
      </ul>
    </>
  );
}
