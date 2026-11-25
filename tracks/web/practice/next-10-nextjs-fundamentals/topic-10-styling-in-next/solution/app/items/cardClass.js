/** The class names for an item's card: the card style, plus the sold-out style when none are left. */
export function cardClass(styles, item) {
  return item.stock === 0 ? `${styles.card} ${styles.soldOut}` : styles.card;
}
