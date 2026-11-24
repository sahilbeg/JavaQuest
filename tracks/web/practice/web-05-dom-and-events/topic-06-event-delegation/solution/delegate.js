// One listener per list, instead of one per button.

/** One click listener on the list: a click inside an .add button calls onAdd with its card's data-id. */
export function listenForAdds(list, onAdd) {
  list.addEventListener("click", (event) => {
    const button = event.target.closest(".add");
    if (button === null) {
      return;
    }
    onAdd(button.closest(".product").dataset.id);
  });
}

/** One click listener on the lines: a button[data-action] calls actions[action] with its line's data-id. */
export function listenForLineActions(lines, actions) {
  lines.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (button === null) {
      return;
    }
    const line = button.closest("[data-id]");
    const run = actions[button.dataset.action];
    if (line !== null && run !== undefined) {
      run(line.dataset.id);
    }
  });
}
