// reconcile: bring a list in line with new items, keeping the rows whose keys are still there.

/**
 * After this runs, list holds exactly one row per item, in the items' order. A row whose key is still there is the
 * same element as before, so its ticks, typed text and focus survive.
 */
export function reconcile(list, items, { key, create, update }) {
  const old = new Map();
  for (const row of list.children) {
    old.set(row.dataset.key, row);
  }

  items.forEach((item, index) => {
    const k = String(key(item));
    let row = old.get(k);
    if (row === undefined) {
      row = create(item);
      row.dataset.key = k;
    } else {
      old.delete(k);
    }
    update(row, item);
    if (list.children[index] !== row) {
      list.insertBefore(row, list.children[index] ?? null);
    }
  });

  for (const row of old.values()) {
    row.remove();
  }
}
