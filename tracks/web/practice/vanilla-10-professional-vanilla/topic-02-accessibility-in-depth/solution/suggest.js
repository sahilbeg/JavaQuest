// Search suggestions that work with a keyboard and a screen reader: a combobox with a listbox.

/** The names that contain the typed words, ignoring capitals and spaces around them. Blank text matches nothing. */
export function matchNames(names, text) {
  const words = text.trim().toLowerCase();
  if (words === "") {
    return [];
  }
  return names.filter((name) => name.toLowerCase().includes(words));
}

/** What the status line says after typing: "", "No suggestions", "1 suggestion" or "3 suggestions". */
export function statusMessage(text, count) {
  if (text.trim() === "") {
    return "";
  }
  if (count === 0) {
    return "No suggestions";
  }
  return count === 1 ? "1 suggestion" : `${count} suggestions`;
}

/** Wires the input (role=combobox), the listbox and the status line. onPick(name) is called when a suggestion is chosen. */
export function createSuggestions({ input, listbox, status, names, onPick }) {
  let state = { matches: [], active: -1, message: "" };

  function render() {
    const open = state.matches.length > 0;
    listbox.replaceChildren(
      ...state.matches.map((name, index) => {
        const option = document.createElement("li");
        option.id = `${listbox.id}-${index}`;
        option.setAttribute("role", "option");
        option.setAttribute("aria-selected", String(index === state.active));
        option.textContent = name;
        return option;
      }),
    );
    listbox.hidden = !open;
    input.setAttribute("aria-expanded", String(open));
    if (state.active >= 0) {
      input.setAttribute("aria-activedescendant", `${listbox.id}-${state.active}`);
    } else {
      input.removeAttribute("aria-activedescendant");
    }
    status.textContent = state.message;
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
    const count = state.matches.length;
    if (count === 0) {
      return;
    }
    if (event.key === "ArrowDown") {
      event.preventDefault();
      update({ active: (state.active + 1) % count });
    } else if (event.key === "ArrowUp") {
      event.preventDefault();
      update({ active: state.active <= 0 ? count - 1 : state.active - 1 });
    } else if (event.key === "Enter" && state.active >= 0) {
      event.preventDefault();
      pick(state.matches[state.active]);
    } else if (event.key === "Escape") {
      update({ matches: [], active: -1, message: "" });
    }
  });

  listbox.addEventListener("click", (event) => {
    const option = event.target.closest('[role="option"]');
    if (option !== null) {
      pick(option.textContent);
    }
  });

  render();
}
