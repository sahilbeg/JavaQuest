## Hook

Dhaaga has a style guide: the exact Marigold, the exact Indigo, how round the buttons are. Every page follows it, so the store feels like one shop, not five.

In React, the look still comes from CSS. What changes is how a component picks its styles: from its props and state, on every render.

## Concept

### Three ways to style a component

1. **A CSS file and `className`.** You write normal CSS rules and give elements class names. This is the most common way.
2. **The `style` prop.** You pass an object of styles straight to one element. It suits values that come from data, like a swatch's colour.
3. **Tools teams add**, such as CSS Modules. They are covered briefly at the end.

### Syntax: importing a CSS file

```jsx
import "./style.css";
```

In a Vite project, you import a stylesheet from JavaScript, usually once in `main.jsx`. The build tool adds it to the page. Its rules apply to the whole page, like a `<link>` in HTML.

### Syntax: className

```jsx
<button type="button" className="chip">Mint</button>
```

`className` is a string of class names separated by spaces, exactly like `class` in HTML. The CSS rule `.chip { ... }` then styles every element with that class.

### Syntax: a class that depends on state

```jsx
<button type="button" className={active ? "tab tab--active" : "tab"}>Orders</button>
```

Because `className` takes any string, you can build it with JavaScript. The element always has `tab`. It also gets `tab--active` while `active` is true.

```callout jargon
A **modifier class** is an extra class for one state or version of something, like `tab--active` or `price--sale`. The `--` is a common naming habit that says "a variant of tab".
```

Watch class names being built, the way a component builds them on each render:

```animate
title: Building class names from state
kind: code
language: js
code: |
  function tabClass(active) {
    return active ? "tab tab--active" : "tab";
  }
  console.log(tabClass(false));
  console.log(tabClass(true));
  const parts = ["badge", true && "badge--new", false && "badge--sold"];
  console.log(parts.filter(Boolean).join(" "));
steps:
  - line: 4
    say: The tab is not active, so the function picks the second string.
  - line: 1
    vars: { active: false }
    say: active is false.
  - line: 4
    out: "tab"
    say: Only the base class. The CSS for .tab applies, and .tab--active does not.
  - line: 5
    vars: { active: true }
    say: Now the tab is active.
  - line: 5
    out: "tab tab--active"
    say: Two classes, with a space between them. The element matches both rules.
  - line: 6
    vars: { parts: '["badge", "badge--new", false]' }
    say: Another way, for several optional classes. Each && gives a class name or false.
  - line: 7
    out: "badge badge--new"
    say: filter(Boolean) drops the false, and join(" ") puts spaces between the rest.
```

Then the CSS does the rest. React only changes the class, and the browser applies whichever rules match:

```animate
title: State picks the class, CSS picks the look
kind: scene
nodes:
  - { id: state, label: "selected", emoji: "🧠", value: "false" }
  - { id: comp, label: "ColourChip()", emoji: "🧩" }
  - { id: el, label: "<button>", emoji: "🔘", value: "class=\"chip\"" }
  - { id: css, label: "style.css", emoji: "🎨" }
steps:
  - say: The Mint chip is not picked. Its button has only the chip class, so only the .chip rule styles it.
    focus: [el, css]
  - say: The customer taps it, and the parent's state changes. The chip re-renders with selected true.
    send: { from: state, to: comp, label: "selected = true" }
    set: { state: "true" }
  - say: The component builds a new class string and React updates just that attribute.
    send: { from: comp, to: el, label: "className" }
    set: { el: "class=\"chip chip--selected\"" }
  - say: Now the .chip--selected rule matches too, and the browser draws the Indigo border. No style was changed by hand.
    send: { from: css, to: el, label: ".chip--selected" }
```

### Syntax: the style prop

```jsx
<span className="swatch" style={{ backgroundColor: "#F2A81D", width: 24 }} />
```

The `style` prop takes a JavaScript object, so there are two pairs of braces: one for "JavaScript here", one for the object.

- Property names are camelCase: `backgroundColor`, not `background-color`.
- Values are strings, like `"#F2A81D"`. A plain number means pixels for sizes, so `width: 24` is `24px`.

Use `style` for values that come from data, like the colour of a swatch or the width of a progress bar. For everything else, a class is easier to change and reuse.

### Styling from attributes

```css
.chip[aria-pressed="true"] {
  border-color: #2e3a8c;
}
```

If a button already says `aria-pressed={selected}` for screen readers, CSS can select on that attribute too. Either way works. A modifier class is easier to read in tests and in DevTools.

### CSS Modules and other tools

```jsx
import styles from "./Chip.module.css";

<button type="button" className={styles.chip}>Mint</button>
```

```callout jargon
**CSS Modules** are CSS files whose class names the build tool makes unique, such as `chip_x7f2`. Two components can both use `.chip` without clashing. Vite supports them for any file ending in `.module.css`.
```

Teams also use tools like Tailwind, which styles with many small ready-made classes. They all end the same way: a class name or a style on an element.

## Worked example

The packing team needs a board for the Diwali rush, with tabs and a progress bar.

```jsx
export function Tab({ label, active, onClick }) {
  return (
    <button type="button" className={active ? "tab tab--active" : "tab"} aria-pressed={active} onClick={onClick}>
      {label}
    </button>
  );
}

export function PackedBar({ packed, total }) {
  const percent = total === 0 ? 0 : Math.round((packed / total) * 100);
  return (
    <div className="bar" role="img" aria-label={`${percent}% of orders packed`}>
      <div className="bar__fill" style={{ width: `${percent}%` }} />
    </div>
  );
}
```

```css
.tab { border: 2px solid transparent; }
.tab--active { border-color: #2e3a8c; font-weight: 700; }
.bar { background: #faf7f0; height: 12px; }
.bar__fill { background: #f2a81d; height: 100%; }
```

Line by line:

- `Tab` picks its class from the `active` prop. All the looks live in CSS.
- `PackedBar` works out a percentage. That number comes from data, so the width goes in `style`.
- `` `${percent}%` `` is a string like `"60%"`. A plain number would mean pixels, which is not what a progress bar wants.
- `role="img"` and `aria-label` let a screen reader say how far packing has got.

## Try it

Run this. Then add `"badge--gift"` with a condition of `true` to the list and run it again.

```js run
const sold = false;
const isNew = true;
const classes = ["badge", isNew && "badge--new", sold && "badge--sold"]
  .filter(Boolean)
  .join(" ");
console.log(classes);
```

It prints `badge badge--new`. This little pattern builds a class string from any number of conditions.

## Recap

- Import a CSS file once, in `main.jsx`, and give elements a `className`.
- Build `className` from props and state: `selected ? "chip chip--selected" : "chip"`. Keep the space between names.
- The `style` prop takes an object with camelCase keys, like `{{ backgroundColor: HEX[colour] }}`. Use it for values that come from data.
- CSS Modules and similar tools make class names unique per component, but the idea is the same.

## Common mistakes

**1. Passing a string to style.**

```jsx
<span style="background-color: #F2A81D" />
```

React stops with an error saying the `style` prop expects an object, not a string. Write `style={{ backgroundColor: "#F2A81D" }}`.

**2. CSS spelling inside a style object.**

```jsx
<span style={{ "background-color": "#F2A81D" }} />
```

The console warns about an unsupported style property and suggests `backgroundColor`. Style keys are camelCase in JavaScript.

**3. Gluing class names together.**

```jsx
<button type="button" className={"chip" + (selected ? "chip--selected" : "")}>Mint</button>
```

When selected, the class is `chipchip--selected`, a single name that matches no rule, so the chip loses all its styles. Put a space before the modifier: `" chip--selected"`, or use the ternary with two full strings.
