import Link from "next/link";
import { listItems } from "../lib/store.js";
import { formatRupees } from "../lib/money.js";

// The catalogue at /: a server component that reads the store directly.
export default async function HomePage() {
  // TODO 3: under the h1, list every item in a <ul>, in catalogue order. Each <li> has a Link to /items/<id> named by
  // the item's name, its price like ₹450.00, and the words "Sold out" when its stock is 0.
  return <h1>Dhaaga & Co.</h1>;
}
