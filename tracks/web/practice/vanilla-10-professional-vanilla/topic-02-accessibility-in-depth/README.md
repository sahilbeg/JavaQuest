# Accessibility in depth: focus and keyboards

The search box shows suggestions, but only for mouse users. Make it a real **combobox** that works with the keyboard and a screen reader. Change `src/suggest.js`. The HTML is ready: an `<input role="combobox">`, a `<ul role="listbox">` and a `<p role="status">`.

1. **TODO 1:** `matchNames(names, text)` returns the names that contain the text, ignoring capitals and spaces around it. Blank text (or only spaces) matches nothing: `[]`.
2. **TODO 2:** `statusMessage(text, count)` returns `""` for blank text, `"No suggestions"` for 0, `"1 suggestion"` for 1, and `"2 suggestions"` and so on for more.
3. **TODO 3:** `render()` draws everything from `state`:
   - one `<li role="option">` per match, with `id` `suggestions-0`, `suggestions-1`... (the listbox's id, a dash, the index), the name as its text, and `aria-selected` `"true"` on the active option, `"false"` on the rest;
   - `listbox.hidden` when there are no matches, and the input's `aria-expanded` `"true"` or `"false"`;
   - the input's `aria-activedescendant` set to the active option's id, or **removed** when nothing is active (`state.active` is `-1`);
   - the status line's text from `state.message`.
4. **TODO 4:** in the `keydown` listener, do nothing when there are no matches. **ArrowDown** makes the next option active, wrapping from the last to the first. **ArrowUp** makes the one before active; from nothing or the first option, it goes to the last. Call `event.preventDefault()` for both arrows.
5. **TODO 5:** **Enter** with an active option picks it (call `pick`, and `preventDefault`). Enter with nothing active does nothing. **Escape** closes the list (no matches, nothing active, empty message) and keeps the typed text.

Focus never leaves the input. The active option is only pointed at, with `aria-activedescendant`. Clicking an option already works.

In the preview, type "coaster", then use the arrows, Enter and Escape without touching the mouse.
