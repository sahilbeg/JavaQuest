// reconcile: bring a list in line with new items, keeping the rows whose keys are still there.

/**
 * After this runs, list holds exactly one row per item, in the items' order. A row whose key is still there is the
 * same element as before, so its ticks, typed text and focus survive.
 */
export function reconcile(list, items, { key, create, update }) {
  // TODO 1: remember the rows already in list, in a Map from row.dataset.key to the row
  // TODO 2: for each item, reuse the row with its key (take it out of the Map), or create(item) and set its data-key
  // TODO 3: update(row, item) for every row; then, if list.children[index] isn't this row,
  //         list.insertBefore(row, list.children[index] ?? null)
  // TODO 4: remove the rows still left in the Map

  // For now it redraws everything, which throws every row away:
  list.replaceChildren(...items.map((item) => {
    const row = create(item);
    row.dataset.key = key(item);
    update(row, item);
    return row;
  }));
}
