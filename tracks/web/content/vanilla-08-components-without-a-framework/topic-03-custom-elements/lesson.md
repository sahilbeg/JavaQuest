## Hook

When a new word becomes popular, the dictionary adds it. After that, everyone can use it, and everyone means the same thing.

Browsers have a dictionary of tags: `<button>`, `<img>`, `<ul>`. A **custom element** adds your own word to it. Once you teach the browser what `<stock-badge>` means, every page can write `<stock-badge count="3">`, and every badge behaves the same way.

## Concept

### Copies drift; one tag doesn't

The stock badge was drawn by three different functions, in three places. Each one was fixed at a different time, so they disagree. A component that lives in a function helps. A component that is a real tag helps even more: the HTML says `<stock-badge count="3">`, and the badge does the rest.

```callout jargon
A **custom element** is an HTML tag you define yourself, with a JavaScript class that says how it behaves. The browser treats it like any built-in tag.
```

### Syntax: a custom element class

```js
class StockBadge extends HTMLElement {
  connectedCallback() {
    this.textContent = "In stock";
  }
}
```

- The class **extends** `HTMLElement`, so every badge is a real element. It has `textContent`, `classList`, `setAttribute` and the rest.
- Inside the class, `this` is the badge element itself.
- Methods with special names, like `connectedCallback`, are called by the browser at the right moment.

### Syntax: customElements.define

```js
customElements.define("stock-badge", StockBadge);
```

- It links the tag name to the class, for the whole page.
- The name **must contain a dash**, like `stock-badge` or `dhaaga-card`. Built-in tags never have one, so yours can never clash with a future one.
- You can define a name only once. A second `define` with the same name throws an error, so modules often check first: `if (!customElements.get("stock-badge"))`.

```callout jargon
The **custom element registry** is the browser's list of your tags. `customElements.define` adds to it, and `customElements.get(name)` looks a name up.
```

### Lifecycle callbacks

The browser calls these methods for you, at fixed moments in the element's life:

| Method | When it runs | Use it to |
|---|---|---|
| `constructor()` | the element is created | set up private fields; always call `super()` first |
| `connectedCallback()` | the element joins the page | draw it, start listening |
| `disconnectedCallback()` | the element leaves the page | stop timers and listeners |
| `attributeChangedCallback(name, oldValue, newValue)` | a watched attribute changes | draw again |

```callout jargon
**Lifecycle callbacks** are methods the browser calls at moments in an element's life: when it's made, added to the page, removed, or changed.
```

```animate
title: A badge's life on the page
kind: scene
nodes:
  - { id: code, label: Your code, emoji: "⌨️", row: 0, col: 0 }
  - { id: badge, label: "<stock-badge>", emoji: "🏷️", value: "not on the page", row: 0, col: 2 }
  - { id: page, label: The page, emoji: "📄", row: 0, col: 4 }
steps:
  - say: createElement("stock-badge") makes a badge. The browser runs the constructor. Nothing is drawn yet.
    send: { from: code, to: badge, label: "createElement" }
    set: { badge: "constructor ran" }
  - say: The code sets count to 3. count is watched, so attributeChangedCallback runs with null and "3".
    send: { from: code, to: badge, label: "setAttribute('count', '3')" }
    set: { badge: "Only 3 left" }
  - say: The badge joins the page. connectedCallback runs, and the badge draws itself.
    send: { from: badge, to: page, label: append }
    set: { badge: "on the page" }
  - say: A sale happens and the count becomes 0. attributeChangedCallback runs again, with "3" and "0".
    send: { from: code, to: badge, label: "setAttribute('count', '0')" }
    set: { badge: "Sold out" }
  - say: The badge is removed. disconnectedCallback runs, the place to clean up.
    send: { from: page, to: badge, label: remove, back: true }
    set: { badge: "off the page" }
```

### Syntax: observedAttributes

```js
class StockBadge extends HTMLElement {
  static observedAttributes = ["count"];

  attributeChangedCallback(name, oldValue, newValue) {
    this.render();
  }
}
```

- The browser calls `attributeChangedCallback` **only** for the attributes listed in `observedAttributes`.
- `static` means the list belongs to the class, not to each badge.
- Attribute values are always strings or `null`. `count="3"` arrives as `"3"`.

### Attributes and properties

HTML can only set **attributes**, which are text. JavaScript prefers **properties**, which can be numbers. A good custom element offers both, kept in step with a getter and a setter:

```js
class StockBadge extends HTMLElement {
  get count() {
    return Number(this.getAttribute("count") ?? 0);
  }

  set count(value) {
    this.setAttribute("count", String(value));
  }
}
```

- `badge.count` reads the attribute and turns it into a number.
- `badge.count = 7` writes the attribute, which runs `attributeChangedCallback`, which draws again.
- So HTML, `setAttribute` and the property all lead to the same place.

```callout jargon
Keeping a property and an attribute in step like this is called **reflecting**: the property reflects the attribute.
```

### Upgrades: tags written before define

The page may write `<stock-badge count="2">` before your script defines it. The browser keeps it as a plain, empty element. When `define` runs, the browser **upgrades** every badge already on the page: it runs the constructor and the callbacks, and each badge draws itself.

## Worked example

The Aundh shop's site shows whether the shop is open. The day comes in as an attribute:

```js
const OPEN_DAYS = ["Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

class ShopStatus extends HTMLElement {
  static observedAttributes = ["day"];

  get day() {
    return this.getAttribute("day") ?? "";
  }

  set day(value) {
    this.setAttribute("day", value);
  }

  connectedCallback() {
    this.render();
  }

  attributeChangedCallback() {
    this.render();
  }

  render() {
    const open = OPEN_DAYS.includes(this.day);
    this.classList.toggle("closed", !open);
    this.textContent = open ? "Open today, 11:00 to 21:00" : "Closed today";
  }
}

if (!customElements.get("shop-status")) {
  customElements.define("shop-status", ShopStatus);
}
```

1. `observedAttributes` watches `day`, so a change redraws the status.
2. The getter and setter reflect `day` between the property and the attribute.
3. `render` does all the drawing, from the attribute alone. Both callbacks only call it.
4. `classList.toggle("closed", !open)` adds the class on Mondays and removes it on other days.
5. The `define` is guarded, so loading the module twice can't throw.

## Try it

Node has no custom elements, so this small `FakeElement` acts like the browser: it calls `attributeChangedCallback` only for watched attributes. Run it, then add `"title"` to `observedAttributes` and run it again.

```js run
class FakeElement {
  #attributes = new Map();

  setAttribute(name, value) {
    const oldValue = this.#attributes.get(name) ?? null;
    this.#attributes.set(name, value);
    if (this.constructor.observedAttributes.includes(name)) {
      this.attributeChangedCallback(name, oldValue, value);
    }
  }
}

class StockBadge extends FakeElement {
  static observedAttributes = ["count"];

  attributeChangedCallback(name, oldValue, newValue) {
    console.log(`${name}: ${oldValue} -> ${newValue}`);
  }
}

const badge = new StockBadge();
badge.setAttribute("count", "3");
badge.setAttribute("title", "Golu");
badge.setAttribute("count", "0");
```

It prints `count: null -> 3`, then `count: 3 -> 0`. The `title` change is ignored, because `title` isn't watched.

## Recap

- A **custom element** is a class that extends `HTMLElement`, registered with `customElements.define("name-with-dash", Class)`, once.
- The browser calls **lifecycle callbacks**: the constructor, `connectedCallback`, `disconnectedCallback` and `attributeChangedCallback`.
- `static observedAttributes` lists the attributes whose changes call `attributeChangedCallback`.
- Attributes are text; properties can be numbers. A getter and setter **reflect** one into the other.
- Tags on the page before `define` are **upgraded** when it runs.

## Common mistakes

**1. A name without a dash.**

```js
customElements.define("stockbadge", StockBadge);
```

Chrome throws `SyntaxError: Failed to execute 'define' on 'CustomElementRegistry': "stockbadge" is not a valid custom element name`. Custom tag names must contain a dash: `stock-badge`.

**2. Forgetting observedAttributes.**

```js
class StockBadge extends HTMLElement {
  attributeChangedCallback() {
    this.render();
  }
}
```

The badge draws once and then never changes, because the browser only calls `attributeChangedCallback` for listed attributes. Add `static observedAttributes = ["count"];`.

**3. Defining the same name twice.**

```js
customElements.define("stock-badge", StockBadge);
customElements.define("stock-badge", StockBadge);
```

The second line throws a `NotSupportedError`, because the name is already taken. Define once, or guard it with `customElements.get("stock-badge")`.

**4. Forgetting that attributes are strings.**

```js
if (this.getAttribute("count") === 0) {
  this.textContent = "Sold out";
}
```

`getAttribute` returns `"0"`, a string, so `=== 0` is never true, and the last Golu never shows "Sold out". Convert first: `Number(this.getAttribute("count")) === 0`, or use the `count` getter.
