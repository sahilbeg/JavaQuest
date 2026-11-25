import styles from "./ItemCard.module.css";
import { cardClass } from "./cardClass.js";
import { rupees } from "../../lib/money.js";

/** One item in the shop's grid. */
export function ItemCard({ item }) {
  // TODO 5: use the module's class names, not plain text:
  // cardClass(styles, item) on the <article>, and styles.price on the price.
  return (
    <article className="card">
      <h2>{item.name}</h2>
      <p className="price">{rupees(item.paise)}</p>
    </article>
  );
}
