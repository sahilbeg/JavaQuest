// One listener per list, instead of one per button.

/** One click listener on the list: a click inside an .add button calls onAdd with its card's data-id. */
export function listenForAdds(list, onAdd) {
  // TODO 1: add one click listener to list; find the button with event.target.closest(".add")
  // TODO 2: return early when there is no such button; otherwise call onAdd with button.closest(".product").dataset.id
}

/** One click listener on the lines: a button[data-action] calls actions[action] with its line's data-id. */
export function listenForLineActions(lines, actions) {
  // TODO 3: add one click listener to lines; find the button with closest("button[data-action]") and its line with closest("[data-id]")
  // TODO 4: look up actions[button.dataset.action]; call it with the line's id only when both exist
}
