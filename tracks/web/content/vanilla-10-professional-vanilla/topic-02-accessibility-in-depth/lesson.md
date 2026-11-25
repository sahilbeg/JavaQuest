## Hook

Close your eyes and put your mouse in a drawer. Now buy a coaster set on dhaaga.in.

You'd press Tab to move around, Enter to press buttons, and the arrow keys inside lists. A screen reader would tell you where you are. If the page forgets either of those, you're stuck, however good the page looks.

Many customers shop exactly like this: people who are blind, people with a shaky hand or a broken wrist, and power users who just like the keyboard. Making a page work for them is part of making it work.

## Concept

### Who uses the keyboard

You met labels, alt text and landmarks in [Accessibility](/t/web-01-html/topic-09-accessibility), and an accessible tab list in [Accessible widgets](/t/vanilla-08-components-without-a-framework/topic-10-accessible-widgets). This ticket goes deeper into the two things that break most often: **focus** and **keys**.

```callout jargon
**Assistive technology** is anything that helps a disabled person use a computer. A **screen reader** is one kind: it reads the page out loud and is driven by the keyboard. Common ones are NVDA and JAWS on Windows, VoiceOver on Apple devices, and TalkBack on Android.
```

A screen reader doesn't see your page. It reads the **accessibility tree**: every element's role, name and state, like "button, Add Golu the elephant to cart" or "list box, collapsed".

### Focus and the Tab order

```callout jargon
**Focus** is the one element that keyboard presses go to right now. Pressing **Tab** moves focus to the next focusable element, and **Shift+Tab** to the one before. The **Tab order** is the order they come in.
```

Links, buttons and form fields can take focus on their own. The Tab order follows the order in the HTML, so a sensible HTML order gives a sensible Tab order for free.

### Syntax: tabindex

```html
<div class="card" tabindex="0">Can be reached with Tab</div>
<h2 id="cart-heading" tabindex="-1">Your cart</h2>
```

- `tabindex="0"` puts an element into the Tab order, in its HTML position.
- `tabindex="-1"` lets code focus it with `element.focus()`, but Tab skips it. Use it for headings and panels you move focus to.
- A positive `tabindex`, like `"3"`, jumps the queue. It's always wrong: the order becomes a puzzle nobody can maintain.

### Visible focus

A keyboard user must always see where focus is. Browsers draw a focus ring, and the most common accessibility bug is deleting it with `outline: none`.

```css
button:focus-visible,
a:focus-visible {
  outline: 3px solid #2E3A8C;
  outline-offset: 2px;
}
```

```callout jargon
**:focus-visible** matches a focused element only when the browser thinks a ring is useful: after a key press, yes; after a mouse click on a button, usually not. So keyboard users get a clear ring, and mouse users aren't bothered by one.
```

### Keys every widget should answer

Customers expect the same keys to do the same things on every website. These come from the **WAI-ARIA Authoring Practices**, the shared rule book for widgets.

| Key | What it should do |
|---|---|
| Tab / Shift+Tab | Move to the next or previous control |
| Enter | Follow a link, press a button, choose an option |
| Space | Press a button, tick a checkbox |
| Escape | Close whatever popped up, and put focus back |
| Arrow keys | Move **inside** one widget: a list of options, tabs, a menu, a radio group |
| Home / End | Jump to the first or last item inside the widget |

Real `<button>`, `<a href>`, `<input>` and `<select>` elements answer Tab, Enter and Space for free. That's the best reason to use them. For a widget you build yourself, you write the arrow keys and Escape.

### Moving focus on purpose

When your code changes the page, focus can be left in a bad place. Your job is to move it somewhere sensible. This is called **focus management**.

| What happened | Where focus should go |
|---|---|
| A dialog or drawer opened | Into it, usually its first control |
| It closed | Back to the button that opened it |
| The focused item was deleted | The next item's button, or the list's heading |
| A single-page app changed screens | The new screen's heading |

The deleted-item case is easy to miss. If the focused button is removed, focus falls back to the very top of the page. A screen reader user hears nothing, and has to Tab through the whole header again.

```animate
title: Removing a saved item without losing focus
kind: scene
nodes:
  - { id: golu, label: "Remove Golu", emoji: "🐘", row: 0, col: 0 }
  - { id: bag, label: "Remove tote bag", emoji: "👜", row: 0, col: 2 }
  - { id: heading, label: "Saved items heading", emoji: "📌", row: 0, col: 4 }
  - { id: body, label: Top of the page, emoji: "⬆️", row: 2, col: 2 }
steps:
  - say: A keyboard user has focus on "Remove Golu" and presses Enter.
    focus: [golu]
  - say: Golu's item and its button are removed from the page. The element with focus no longer exists.
    hide: [golu]
  - say: If the code does nothing, focus falls back to the top of the page. The user is lost.
    send: { from: bag, to: body, label: "focus lost" }
    focus: [body]
  - say: Instead, the code calls focus() on the next item's Remove button. The user carries on from the same spot.
    send: { from: body, to: bag, label: "focus()" }
    focus: [bag]
  - say: When the last item goes, there's no next button. Focus moves to the list's heading, which has tabindex -1.
    hide: [bag]
    focus: [heading]
```

### Syntax: aria-activedescendant

```html
<input id="search" role="combobox" aria-expanded="true" aria-controls="suggestions" aria-activedescendant="suggestions-1">
<ul id="suggestions" role="listbox">
  <li id="suggestions-0" role="option" aria-selected="false">Marigold coaster set</li>
  <li id="suggestions-1" role="option" aria-selected="true">Indigo coaster set</li>
</ul>
```

Some widgets need focus to stay in one place while the arrows move a highlight. A search box with suggestions is the classic one: the customer must keep typing in the input.

```callout jargon
**aria-activedescendant** is set on the focused element and holds the id of the option that is "active" inside it. Real focus stays put, but the screen reader reads the active option out as if it were focused. A **combobox** is an input with a pop-up list of choices, and a **listbox** is that list.
```

- The input has `role="combobox"`, `aria-controls` naming the list, and `aria-expanded` saying whether it's open.
- Each option has `role="option"` and a unique `id`. The active one has `aria-selected="true"`.
- When nothing is active, remove `aria-activedescendant` completely. An empty or wrong id confuses screen readers.

### Live regions: saying what changed

When you type into the suggestion box, the list appears visually. A screen reader user needs to hear that something appeared.

```callout jargon
A **live region** is an element whose changes the screen reader reads out, even though focus is elsewhere. `role="status"` is a polite one: it waits until the reader finishes speaking. `role="alert"` interrupts, so keep it for errors.
```

```html
<p id="search-status" role="status"></p>
```

Put the live region in the HTML from the start, empty. Then change only its text: "2 suggestions". A region added to the page together with its text is often not read at all.

## Worked example

Here is the saved-items list from the animation. Each item has a Remove button, and the list has a heading with `tabindex="-1"`.

```js
function removeSaved(list, heading, button) {
  const item = button.closest("li");
  const next = item.nextElementSibling ?? item.previousElementSibling;
  item.remove();
  if (next !== null) {
    next.querySelector("button").focus();
  } else {
    heading.focus();
  }
}

const savedList = document.querySelector("#saved");
const savedHeading = document.querySelector("#saved-heading");
savedList.addEventListener("click", (event) => {
  const button = event.target.closest("button.remove");
  if (button !== null) {
    removeSaved(savedList, savedHeading, button);
  }
});
```

Line by line:

- `next` is found **before** the item is removed, while the neighbours are still easy to reach. If there's no next item, the one before will do.
- `item.remove()` takes the focused button out of the page.
- Focus moves to the neighbour's button, so the user can keep removing with Enter. With no neighbours left, the heading takes focus and is read out.
- One listener on the list handles every Remove button, as in [Event delegation](/t/web-05-dom-and-events/topic-06-event-delegation).

## Try it

Keyboard logic is easiest to get right as a plain function, with no page at all. This one works out the next active option after a key press, for a list of 3. Run it.

```js run
function nextIndex(key, current, count) {
  if (count === 0) return -1;
  if (key === "ArrowDown") return (current + 1) % count;
  if (key === "ArrowUp") return current <= 0 ? count - 1 : current - 1;
  if (key === "Home") return 0;
  if (key === "End") return count - 1;
  return current;
}

let active = -1;
for (const key of ["ArrowDown", "ArrowDown", "ArrowDown", "ArrowUp", "End", "Home", "ArrowUp"]) {
  active = nextIndex(key, active, 3);
  console.log(key.padEnd(9) + " -> " + active);
}
```

It prints:

```text
ArrowDown -> 0
ArrowDown -> 1
ArrowDown -> 2
ArrowUp   -> 1
End       -> 2
Home      -> 0
ArrowUp   -> 2
```

`-1` means "nothing active yet". The first ArrowDown makes option 0 active, and ArrowUp from the first option wraps to the last.

1. Add one more `"ArrowDown"` at the end. Which option becomes active, and why?
2. Change the count to 0, as if no suggestions matched. What does every key give now?

## Recap

- Everything must work with Tab, Shift+Tab, Enter, Space, Escape and the arrows. Real buttons, links and fields give you most of it for free.
- Use `tabindex="0"` to join the Tab order and `tabindex="-1"` for things code focuses. Never a positive number. Keep a visible focus ring with `:focus-visible`.
- Move focus on purpose: into a dialog and back, to a neighbour when the focused item is deleted, to the new heading when the screen changes.
- In a combobox, focus stays in the input, and `aria-activedescendant` points at the active option's id. Remove it when nothing is active.
- Announce changes with a live region that is already in the page: `role="status"` for news, `role="alert"` for errors.

## Common mistakes

**1. A clickable div.**

```html
<div class="add" onclick="addToCart('DC-1042')">Add to cart</div>
```

Symptom: Tab skips it, Enter does nothing, and the screen reader reads plain text, not a button. Fix: `<button type="button" class="add">Add to cart</button>`. A real button is focusable and answers Enter and Space for free.

**2. Removing the focus ring.**

```css
*:focus {
  outline: none;
}
```

Symptom: keyboard users can't see where they are, so every Tab press is a guess. Fix: delete the rule, or replace it with a clear `:focus-visible` style in the brand Indigo.

**3. A positive tabindex.**

```html
<button tabindex="1">Checkout</button>
```

Symptom: the first Tab jumps to Checkout, then focus goes back to the top. The order matches neither the screen nor the HTML. Fix: remove it, and put the HTML in the order you want.

**4. Deleting the focused element and doing nothing.**

```js
document.querySelector("#saved li").remove();
```

Symptom: after pressing Remove, the next Tab starts from the top of the page, and the screen reader says nothing. Fix: before removing, find the neighbour, then `focus()` its button, or the list's heading.

**5. A stale aria-activedescendant.**

```js
const search = document.querySelector("#search");
search.setAttribute("aria-activedescendant", "");
```

Symptom: after the list closes, some screen readers still try to read an option that no longer exists, or read nothing. Fix: `search.removeAttribute("aria-activedescendant")` whenever no option is active.

**6. Adding the live region with its message.**

```js
document.body.insertAdjacentHTML("beforeend", '<p role="status">2 suggestions</p>');
```

Symptom: the screen reader often stays silent, because it hadn't been watching that region yet. Fix: put `<p id="search-status" role="status"></p>` in the HTML from the start, and change only its `textContent`.
