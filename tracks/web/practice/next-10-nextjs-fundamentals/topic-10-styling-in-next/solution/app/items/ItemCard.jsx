import styles from "./ItemCard.module.css";
import { cardClass } from "./cardClass.js";
import { rupees } from "../../lib/money.js";

/** One item in the shop's grid. */
export function ItemCard({ item }) {
  return (
    <article className={cardClass(styles, item)}>
      <h2>{item.name}</h2>
      <p className={styles.price}>{rupees(item.paise)}</p>
    </article>
  );
}
