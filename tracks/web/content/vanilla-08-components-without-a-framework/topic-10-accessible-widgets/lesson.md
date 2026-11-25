## Hook

A lift has buttons for every floor, but it also has Braille on each button and a voice that says "third floor". Without those, a blind passenger can still press buttons, but has no idea which one does what.

Golu's item page has three tabs: Details, Care and Delivery. With a mouse, they look and work like tabs. To a screen reader, they are three loose buttons, and the keyboard keys people expect do nothing. This ticket adds the Braille and the voice.

## Concept

### What a tab widget is

A **tab list** shows one panel at a time, with a row of tabs to switch between them. Every tab list on the web is expected to behave the same way:
- A screen reader says "Care, tab, 2 of 3, selected".
- Tab moves focus **into** the tab list once, onto the selected tab, and the next Tab press moves **out** to the panel.
- Left and Right arrows move between tabs. Home and End jump to the first and last.

People who use a keyboard or a screen reader learn this once and expect it everywhere. Your job is to meet that expectation.

```callout jargon
A **widget** is an interactive control built from several elements, like a tab list, a menu or a slider. **ARIA** (Accessible Rich Internet Applications) is a set of HTML attributes that tell assistive technology what a widget is and what state it's in.
```

### Syntax: ARIA roles

```html
<div role="tablist" aria-label="About Golu the elephant">
  <button role="tab">Details</button>
</div>
<section role="tabpanel">...</section>
```

- `role="tablist"` on the element that holds the tabs.
- `role="tab"` on each tab. It replaces "button" in what the screen reader says.
- `role="tabpanel"` on each panel.
- A role **only changes what is announced**. It adds no behaviour. The keyboard part is still your job.

```callout jargon
A **role** tells assistive technology what kind of thing an element is, like a tab or a dialog. It doesn't change how the element looks or behaves.
```

### Syntax: ARIA states and links

```html
<button role="tab" id="tab-care" aria-selected="true" aria-controls="panel-care">Care</button>
<section role="tabpanel" id="panel-care" aria-labelledby="tab-care" tabindex="0">...</section>
```

- `aria-selected` is the **string** `"true"` on the selected tab and `"false"` on the others.
- `aria-controls` on a tab names the id of its panel.
- `aria-labelledby` on a panel names the id of its tab, so the panel is announced as "Care".
- `tabindex="0"` on a panel lets keyboard users Tab into it, even when it holds only text.

Hide the other panels with the `hidden` attribute, so screen readers skip them too.

### Syntax: roving tabindex

```js
tab.tabIndex = selected ? 0 : -1;
```

- `tabindex="0"`: the element is in the Tab order.
- `tabindex="-1"`: it isn't in the Tab order, but code can still `focus()` it.
- Only the selected tab gets `0`. The others get `-1`. Tab enters the list on the selected tab, and the next Tab leaves the list, instead of stopping at every tab.
- When the selection moves, the `0` moves with it. It "roves".

```callout jargon
**Roving tabindex** keeps exactly one element of a widget in the Tab order, and moves it as the user moves around with the arrow keys.
```

```animate
title: Arrow keys move the one tabindex="0"
kind: scene
nodes:
  - { id: details, label: Details tab, emoji: "📄", value: "selected, tabindex 0", row: 0, col: 0 }
  - { id: care, label: Care tab, emoji: "🧼", value: "tabindex -1", row: 0, col: 2 }
  - { id: delivery, label: Delivery tab, emoji: "🚚", value: "tabindex -1", row: 0, col: 4 }
  - { id: panel, label: Panel, emoji: "📃", value: Details, row: 1, col: 2 }
steps:
  - say: The customer presses Tab. Focus lands on Details, the only tab with tabindex 0.
    focus: [details]
  - say: She presses the right arrow. The keydown listener on the list works out the next tab, Care.
    send: { from: details, to: care, label: ArrowRight }
  - say: Care becomes selected and gets tabindex 0. Details gets -1. Focus moves to Care, and its panel shows.
    set: { details: "tabindex -1", care: "selected, tabindex 0", panel: Care }
    focus: [care, panel]
  - say: Right again, to Delivery, the last tab.
    send: { from: care, to: delivery, label: ArrowRight }
    set: { care: "tabindex -1", delivery: "selected, tabindex 0", panel: Delivery }
  - say: Right once more wraps round to the first tab, Details.
    send: { from: delivery, to: details, label: ArrowRight }
    set: { delivery: "tabindex -1", details: "selected, tabindex 0", panel: Details }
  - say: She presses Tab. Only one tab was in the Tab order, so focus leaves the list and goes into the panel.
    send: { from: details, to: panel, label: Tab }
    focus: [panel]
```

### The keys

| Key | Moves to |
|---|---|
| Right arrow | the next tab, and from the last back to the first |
| Left arrow | the previous tab, and from the first round to the last |
| Home | the first tab |
| End | the last tab |

Moving also **selects** the tab and shows its panel. For each of these keys, call `event.preventDefault()`, so the page doesn't scroll as well. Every other key does nothing, so Tab and Enter keep their normal jobs.

### Syntax: wrapping with %

```js
const next = (current + 1) % tabs.length;
const previous = (current - 1 + tabs.length) % tabs.length;
```

- `%` gives the remainder after dividing. With 3 tabs, `(2 + 1) % 3` is `0`: past the last tab, back to the first.
- Going left from `0` gives `-1`. Adding `tabs.length` first makes it `2`, and `2 % 3` is `2`: the last tab.

### Style from the ARIA state

```css
[role="tab"][aria-selected="true"] { border-bottom: 3px solid #F2A81D; }
```

Draw the selected tab from `aria-selected`, not from a separate class. Then the look and the announcement can never disagree.

### Native first

The first rule of ARIA: if a native HTML element already does the job, use it. A `<button>` is focusable and works with Enter and Space for free; a `<div role="button">` has none of that. ARIA is for widgets HTML doesn't have, like tabs. Even then, build them from real buttons, as this ticket does.

## Worked example

The delivery page lets customers pick a size of gift box with three toggle buttons. Only one can be pressed at a time.

```js
function setupSizes(group) {
  const buttons = [...group.querySelectorAll("button")];
  group.setAttribute("role", "group");

  function choose(index) {
    buttons.forEach((button, i) => {
      button.setAttribute("aria-pressed", String(i === index));
    });
  }

  group.addEventListener("click", (event) => {
    const button = event.target.closest("button");
    if (button !== null) {
      choose(buttons.indexOf(button));
    }
  });

  choose(0);
}
```

1. These are real `<button>` elements, so Tab, Enter and Space already work. No extra keyboard code is needed.
2. `role="group"` tells a screen reader the buttons belong together. The page should also give the group an `aria-label`, like "Gift box size".
3. `aria-pressed` says which one is on, as the string `"true"` or `"false"`.
4. `choose` sets the state on **every** button, so the old one is always switched off. The same pattern sets `aria-selected` on tabs.

## Try it

The arrow-key maths is plain JavaScript. Run it, then add `"End"` to the list of keys.

```js run
const tabs = ["Details", "Care", "Delivery"];

function nextIndex(current, key) {
  if (key === "ArrowRight") {
    return (current + 1) % tabs.length;
  }
  if (key === "ArrowLeft") {
    return (current - 1 + tabs.length) % tabs.length;
  }
  if (key === "Home") {
    return 0;
  }
  if (key === "End") {
    return tabs.length - 1;
  }
  return current;
}

let current = 0;
for (const key of ["ArrowLeft", "ArrowLeft", "Home", "ArrowRight", "x"]) {
  current = nextIndex(current, key);
  console.log(`${key} -> ${tabs[current]}`);
}
```

It prints `ArrowLeft -> Delivery`, `ArrowLeft -> Care`, `Home -> Details`, `ArrowRight -> Care`, and `x -> Care`: the `x` key changes nothing.

## Recap

- A tab list needs **roles**: `tablist`, `tab` and `tabpanel`. Roles change what is announced, not what happens.
- **States and links**: `aria-selected` (`"true"`/`"false"`), `aria-controls` from tab to panel, `aria-labelledby` from panel to tab. Hide other panels with `hidden`.
- **Roving tabindex**: only the selected tab has `tabindex="0"`; the rest have `-1`.
- Arrows move and wrap with `%`; Home and End jump. Each handled key selects, focuses and calls `preventDefault()`.
- Style from the ARIA state, and use native elements first.

## Common mistakes

**1. Adding roles but no keyboard.**

```js
tab.setAttribute("role", "tab");
```

The screen reader now says "tab", so the customer presses the arrow keys, and nothing happens. A role is a promise. Add the keydown handler too.

**2. Setting aria-selected only on the new tab.**

```js
tabs[next].setAttribute("aria-selected", "true");
```

The old tab still says it is selected, so a screen reader hears two selected tabs. Set `aria-selected` on every tab, each time: `"true"` for the chosen one and `"false"` for the rest.

**3. Leaving every tab in the Tab order.**

```js
tab.tabIndex = 0;
```

A keyboard user must press Tab three times to get past the tabs, and can't tell which one is selected. Give `0` only to the selected tab and `-1` to the others.

**4. Selecting without moving focus.**

```js
if (event.key === "ArrowRight") {
  select(current + 1);
}
```

The new tab is selected, but focus stays on the old one, so the next arrow press starts from the wrong place. Call `tabs[next].focus()` too. And `current + 1` runs off the end: wrap it with `%`.
