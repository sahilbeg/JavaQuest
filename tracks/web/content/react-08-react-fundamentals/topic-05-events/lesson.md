## Hook

Before leaving the stall for lunch, the owner leaves a note for the helper: "If the courier comes, hand him the blue bag." The bag is not handed over now. The note says what to do when something happens.

An event handler is that note. You give React a function now, and React calls it later, when the customer taps or types.

## Concept

### Syntax: an event handler

```jsx
function AddButton() {
  function handleClick() {
    console.log("Added to cart");
  }

  return <button type="button" onClick={handleClick}>Add to cart</button>;
}
```

`onClick={handleClick}` hands React the function. React calls it each time the button is clicked.

```callout jargon
An **event** is something that happens on the page, such as a click, a key press or typing. An **event handler** is the function that runs when it happens.
```

Note the missing round brackets. `onClick={handleClick}` passes the function. `onClick={handleClick()}` would call it right away, while drawing, and pass whatever it returns.

```callout tip
Name handlers `handle` plus the event, like `handleClick` or `handleKeyDown`. Name the props that receive them `on` plus the event, like `onAdd` or `onOpen`.
```

### Handlers can see props and state

A handler is written inside the component, so it can use the component's props and state. This is a closure, which you met in JavaScript in depth.

```jsx
function QuantityPicker({ stock }) {
  const [qty, setQty] = useState(1);

  function handlePlus() {
    if (qty < stock) {
      setQty(qty + 1);
    }
  }

  return <button type="button" onClick={handlePlus}>+</button>;
}
```

### Syntax: passing an argument

```jsx
<button type="button" onClick={() => onAdd(id)}>Add to cart</button>
```

Sometimes the function needs a value, like the product's id. Wrap the call in an arrow function. React calls the arrow function on a click, and the arrow function calls `onAdd(id)`.

### Functions as props

`onAdd` above is a prop. The parent passes a function down, and the child calls it when something happens:

```jsx
<CartButton id="DC-1042" onAdd={(id) => console.log(`add ${id}`)} />
```

Data flows down as props. News of events flows back up through function props. This is how a child tells its parent "the customer clicked me".

### Syntax: the event object

```jsx
function handleKeyDown(event) {
  console.log(event.key);          // "Enter", "a", "Escape" ...
  console.log(event.target.value); // the text in the box
}

<input type="search" onKeyDown={handleKeyDown} />
```

React passes your handler one argument: an object that describes what happened.

- `event.key` is the key that was pressed, such as `"Enter"`.
- `event.target` is the element the event happened on. For a text box, `event.target.value` is its text.

```callout jargon
React's **synthetic event** is a wrapper around the browser's own event. It works the same in every browser and has the methods you know, such as `stopPropagation()` and `preventDefault()`.
```

### Events bubble up

A click on a button is also a click on everything around it: the card, the section and the page. The event starts at the button and travels outwards.

```callout jargon
**Bubbling** means an event runs the handler on the element where it happened, then on its parent, then on that parent's parent, all the way up.
```

That is the bug in this ticket. Watch a tap on Add to cart inside a clickable product tile:

```animate
title: One tap, two handlers
kind: scene
nodes:
  - { id: btn, label: "Add to cart button", emoji: "🛒" }
  - { id: tile, label: "Golu's tile (article)", emoji: "🐘" }
  - { id: cart, label: "Cart", emoji: "🧺", value: "empty" }
  - { id: page, label: "Product page", emoji: "📄" }
steps:
  - say: The customer taps Add to cart on Golu's tile.
    focus: [btn]
  - say: The button's onClick runs first and calls onAdd. Golu goes into the cart.
    send: { from: btn, to: cart, label: "onAdd(\"DC-1042\")" }
    set: { cart: "1 item" }
  - say: Then the same click bubbles up to the article around the button. The article has its own onClick.
    send: { from: btn, to: tile, label: "click bubbles up" }
  - say: The tile's handler opens Golu's page. The customer never asked for that, and the new page loses track of the cart.
    send: { from: tile, to: page, label: "onOpen(\"DC-1042\")" }
  - say: The fix is event.stopPropagation() in the button's handler. Then the click stops at the button, and the tile never hears it.
    focus: [btn]
```

### Syntax: stopPropagation

```jsx
function handleClick(event) {
  event.stopPropagation();
  onAdd(id);
}
```

`event.stopPropagation()` stops the bubbling. Handlers further up are not called. Use it only where a parent really must not hear the event, like a button inside a clickable card.

### Syntax: preventDefault

```jsx
function handleSubmit(event) {
  event.preventDefault();
}
```

Some elements do something by themselves. A link goes to a new page, and a form reloads the page when sent. `event.preventDefault()` stops that built-in action. You will use it in every form, in the forms ticket.

### Events you will use most

| Prop | When it runs |
|---|---|
| `onClick` | a click or tap |
| `onChange` | the value of an input changes |
| `onKeyDown` | a key is pressed |
| `onSubmit` | a form is sent |
| `onFocus`, `onBlur` | an element gains or loses focus |

## Worked example

Tapping a product photo opens it big, on a dark background. Tapping the background or pressing Escape closes it. Tapping the photo itself must not close it.

```jsx
export function Lightbox({ photo, alt, onClose }) {
  function handleKeyDown(event) {
    if (event.key === "Escape") {
      onClose();
    }
  }

  return (
    <div className="lightbox" tabIndex={-1} onClick={onClose} onKeyDown={handleKeyDown}>
      <img src={photo} alt={alt} onClick={(event) => event.stopPropagation()} />
    </div>
  );
}
```

Line by line:

- `onClick={onClose}` passes the parent's function straight through. A tap on the background calls it.
- `handleKeyDown` reads `event.key`. Only Escape closes the box. Any other key does nothing.
- `tabIndex={-1}` lets the div receive key presses once it has focus.
- The image's handler stops the click from bubbling, so tapping the photo leaves it open.

## Try it

React's bubbling follows the same path as the browser's. This plain JavaScript model walks a click from the button up to the page. Run it, then set `stopAtButton` to `true` and run it again.

```js run
const stopAtButton = false;
const path = ["button", "article", "main"];
let stopped = false;

for (const element of path) {
  if (stopped) break;
  console.log(`${element} heard the click`);
  if (element === "button" && stopAtButton) {
    stopped = true;
  }
}
```

With `false`, all three elements hear the click. With `true`, only the button does. That is what `stopPropagation()` does.

## Recap

- Pass a function to an event prop: `onClick={handleClick}`, without round brackets.
- To pass a value, wrap the call in an arrow function: `onClick={() => onAdd(id)}`.
- A parent passes functions like `onAdd` down as props, and the child calls them to report events.
- The handler gets an event object: `event.key`, `event.target.value`, `event.stopPropagation()` and `event.preventDefault()`.
- Events bubble up through the elements around them. `stopPropagation()` stops that.

## Common mistakes

**1. Calling the handler instead of passing it.**

```jsx
<button type="button" onClick={onAdd(id)}>Add to cart</button>
```

Every product goes into the cart the moment the page draws, and clicking does nothing more. If `onAdd` changes state, React stops with an error about too many re-renders. Wrap it: `onClick={() => onAdd(id)}`.

**2. Writing the event name in lower case.**

```jsx
<button type="button" onclick={handleClick}>Add to cart</button>
```

Nothing happens on a click, and the console warns about an invalid event handler and suggests `onClick`. React event props are camelCase.

**3. Forgetting that the click bubbles.**

```jsx
<article onClick={() => onOpen(id)}>
  <button type="button" onClick={() => onAdd(id)}>Add to cart</button>
</article>
```

Adding to the cart also opens the product page, because the click reaches the article too. Stop it in the button's handler: `onClick={(event) => { event.stopPropagation(); onAdd(id); }}`.

**4. Reading the key from the wrong place.**

```jsx
function handleKeyDown(event) {
  if (event.target.value === "Enter") {
    onSearch(event.target.value);
  }
}
```

Enter never works, because `event.target.value` is the text in the box, not the key. The key is `event.key`.
