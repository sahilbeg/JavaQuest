# Accessibility in depth: keyboard, focus and ARIA

Two parts of the cart page only work with a mouse. Make them work with a keyboard and a screen reader. Change `src/Disclosure.jsx` and `src/RemoveItemButton.jsx`.

## The help answer: `Disclosure` (TODOs 1–2)

`<Disclosure title="Delivery and returns">...answer...</Disclosure>` shows a title that opens and closes its answer.

- The title is a real `<button type="button">` inside the `<h3>`, not a clickable `<div>`.
- The button has `aria-expanded`: `"false"` when closed, `"true"` when open.
- The button has `aria-controls` set to the panel's id. Use `panelId` from `useId()`.
- The panel is **always** rendered, with `id={panelId}`. When closed it has the `hidden` attribute (`hidden={!open}`).
- Tab reaches the title. Enter and Space open and close it. A real button does this for you.

## The cart's Remove button: `RemoveItemButton` (TODOs 3–6)

`<RemoveItemButton itemName="Golu the elephant" onRemove={...} />` asks before it removes an item.

- The button that opens the dialog shows the word **Remove**, but its accessible name names the item: `aria-label="Remove Golu the elephant"`.
- The dialog box has `role="dialog"`, `aria-modal="true"` and `aria-labelledby` set to its heading's id (`titleId`). So its name is the heading, "Remove Golu the elephant?".
- When the dialog opens, focus moves to **Cancel**, the safe choice.
- **Escape** closes the dialog. So does **Cancel**. Neither removes anything.
- **Yes, remove** calls `onRemove()` once and closes the dialog.
- Whenever the dialog closes, focus goes back to the **Remove** button that opened it.

The checks use only roles, names, `aria-*` attributes, the keyboard and `document.activeElement`. The preview shows both parts on a cart page: try them with Tab, Enter, Space and Escape.
