// Search suggestions that work with a keyboard and a screen reader: a combobox with a listbox.

/** The names that contain the typed words, ignoring capitals and spaces around them. Blank text matches nothing. */
export function matchNames(names, text) {
  // TODO 1: trim and lower-case the text; return [] for blank text, else the names whose lower-case form includes it
  return [];
}

/** What the status line says after typing: "", "No suggestions", "1 suggestion" or "3 suggestions". */
export function statusMessage(text, count) {
  // TODO 2: "" for blank text, "No suggestions" for 0, "1 suggestion" for 1, otherwise `${count} suggestions`
  return "";
}

/** Wires the input (role=combobox), the listbox and the status line. onPick(name) is called when a suggestion is chosen. */
export function createSuggestions({ input, listbox, status, names, onPick }) {
  let state = { matches: [], active: -1, message: "" };

  function render() {
    // TODO 3: draw everything from state:
    //   - one <li role="option"> per match, with id `${listbox.id}-${index}`, aria-selected "true" only on the active one, and the name as text
    //   - listbox.hidden when there are no matches, and the input's aria-expanded "true" or "false"
    //   - the input's aria-activedescendant set to the active option's id, or removed when nothing is active
    //   - status.textContent = state.message
  }

  function update(changes) {
    state = { ...state, ...changes };
    render();
  }

  function pick(name) {
    input.value = name;
    update({ matches: [], active: -1, message: "" });
    onPick(name);
  }

  input.addEventListener("input", () => {
    const matches = matchNames(names, input.value);
    update({ matches, active: -1, message: statusMessage(input.value, matches.length) });
  });

  input.addEventListener("keydown", (event) => {
    // TODO 4: with no matches, do nothing. ArrowDown makes the next option active (the last wraps to the first),
    //         ArrowUp the one before (from none or the first, go to the last). Call event.preventDefault() for both.
    // TODO 5: Enter with an active option picks it (and preventDefault). Escape closes the list: no matches,
    //         nothing active, an empty message. Focus always stays in the input.
  });

  listbox.addEventListener("click", (event) => {
    const option = event.target.closest('[role="option"]');
    if (option !== null) {
      pick(option.textContent);
    }
  });

  render();
}
