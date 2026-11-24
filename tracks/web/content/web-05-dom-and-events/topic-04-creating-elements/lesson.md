## Hook

At the counter, Raju writes each order on a slip. A customer adds a keychain: he writes a new line. They change their mind about the booties: he crosses that line out. They pay: he tears the slip off and starts fresh.

So far your code changed elements that were already in the HTML. A real cart needs more. It must add lines that never existed, and take them away again.

## Concept

### Syntax: createElement

```js
const element = document.createElement("tagName");
```

- It makes a brand-new element object, like `<li></li>`, with nothing inside.
- The tag name has no angle brackets: `"li"`, not `"<li>"`.
- The new element is **not on the page yet**. It floats in memory until you add it somewhere.

```callout jargon
A **detached** element is one that exists in memory but is not part of the page's tree, so it isn't shown. Every element from `createElement` starts detached.
```

You fill it with the tools from the last ticket, while it is still detached:

```js
const li = document.createElement("li");
li.className = "line";
li.dataset.code = "KEY";
li.textContent = "Keychain";
```

Setting `className` is fine here. The element is brand new, so there are no other classes to lose.

### Syntax: append

```js
parent.append(child);
parent.append(child1, " text ", child2);
parent.prepend(child);
```

- `append` puts one or more children at the **end** of the parent. `prepend` puts them at the start.
- A plain string becomes a text node, so `append` can mix elements and text.
- Once appended to an element that is on the page, the child shows up at once.

You will also see `parent.appendChild(child)` in older code. It does the same job, one element at a time.

Watch a line go from memory onto the slip:

```animate
title: From createElement to the screen
kind: scene
nodes:
  - { id: js, label: Your code, emoji: "⚙️", row: 0, col: 0 }
  - { id: li, label: new li, emoji: "🧾", value: empty, row: 1, col: 1 }
  - { id: ul, label: "ul#slip", emoji: "📋", value: "2 lines", row: 0, col: 3 }
  - { id: screen, label: Screen, emoji: "📱", value: "2 lines", row: 1, col: 4 }
links:
  - { from: ul, to: screen }
steps:
  - say: Raju adds a keychain. The code asks the document for a new li element.
    send: { from: js, to: li, label: "createElement('li')" }
    show: [li]
  - say: The li exists, but only in memory. It is detached, so nothing on the screen changes.
    focus: [li, screen]
  - say: The code fills it while it is still detached. The screen still shows 2 lines.
    send: { from: js, to: li, label: "textContent = 'Keychain'" }
    set: { li: Keychain }
  - say: Now append attaches it to the slip, as the last child.
    send: { from: li, to: ul, label: "slip.append(li)" }
    set: { ul: "3 lines" }
    hide: [li]
  - say: The slip is on the page, so the browser draws the new line straight away.
    send: { from: ul, to: screen, label: redraw }
    set: { screen: "3 lines" }
```

### Building a line from small pieces

A real line has parts: the name, the quantity and a Remove button. Make each part, then put them together.

```js
const name = document.createElement("span");
name.className = "name";
name.textContent = "Keychain";

const qty = document.createElement("span");
qty.className = "qty";
qty.textContent = "2";

const li = document.createElement("li");
li.append(name, " × ", qty);
```

Every piece of text goes in through `textContent`, so a name with odd characters can never turn into HTML.

```callout tip
Put the building in a function that **returns** the element, like `function slipLine(item) { ... return li; }`. Then one function decides where it goes, and another decides what it looks like.
```

### Syntax: remove

```js
element.remove();
```

- The element leaves the page, along with everything inside it.
- To remove one line, find it first, then remove it. Check for `null`, because the line may not be there.

```js
const line = slip.querySelector('[data-code="BTY"]');
if (line !== null) {
  line.remove();
}
```

### Syntax: replaceChildren

```js
parent.replaceChildren();               // remove every child
parent.replaceChildren(newA, newB);     // swap all children for these
```

- With no arguments, it empties the element. That is the slip being torn off.
- With arguments, it removes the old children and puts the new ones in, in one step. You will use that to redraw lists from data.

### Why not innerHTML +=

You may see code that adds a line like this:

```js
slip.innerHTML += `<li>${name}</li>`;
```

It looks short, but it does three bad things:
1. It turns the whole list back into a string, then parses it all again. Every old line is rebuilt as a brand-new element.
2. Anything attached to the old elements, like the click handlers you meet in the next ticket, is lost.
3. A name with tags in it becomes real HTML, the XSS risk from the last ticket.

`createElement` with `append` adds one element and touches nothing else.

### An element lives in one place

An element can only have one parent. If you append an element that is already on the page, it **moves**. It is not copied.

```js
const golu = document.querySelector('[data-code="AMI"]');
document.querySelector("#saved").append(golu);   // Golu leaves the slip and joins #saved
```

To show the same item twice, create two elements.

## Worked example

The stall tablet keeps a "yarn to reorder" list. Each line shows a colour and how many grams to buy.

```js
function reorderLine(colour, grams) {
  const li = document.createElement("li");
  li.className = "reorder";
  li.dataset.colour = colour;

  const label = document.createElement("strong");
  label.textContent = colour;

  li.append(label, `: ${grams} g`);
  return li;
}

const list = document.querySelector("#reorder");
list.append(reorderLine("Mint", 500));
list.append(reorderLine("Indigo", 250));

list.querySelector('[data-colour="Mint"]').remove();
console.log(list.children.length);   // 1
```

1. `reorderLine` builds one detached `<li>` with a `<strong>` and some text, and returns it.
2. Each `append` adds a line at the end, so Mint comes before Indigo.
3. Mint's yarn arrives early, so its line is found by `data-colour` and removed.
4. One line is left, Indigo.

## Try it

Open your ticket's preview, then its Console. Build a line by hand:

```js
const slip = document.querySelector("#slip");
const li = document.createElement("li");
li.textContent = "Tote bag";
slip.children.length;
slip.append(li);
slip.children.length;
li.remove();
```

Check the count before and after `append`. Then try `slip.prepend(li)` and see where it lands.

## Recap

- `document.createElement("li")` makes a new, detached element. Fill it, then add it to the page.
- `append` adds children at the end, `prepend` at the start. Strings become text nodes.
- Build bigger pieces from small ones, with all text through `textContent`, inside a function that returns the element.
- `element.remove()` takes an element away. `replaceChildren()` empties a parent, or swaps in new children.
- Avoid `innerHTML +=`: it rebuilds everything, drops listeners and risks XSS.

## Common mistakes

**1. Creating an element and never adding it.**

```js
const li = document.createElement("li");
li.textContent = "Keychain";
```

Nothing shows up, and there is no error. The element is still detached. Add it: `slip.append(li);`.

**2. Angle brackets in createElement.**

```js
const li = document.createElement("<li>");
```

The console shows `InvalidCharacterError: Failed to execute 'createElement' on 'Document': The tag name provided ('<li>') is not a valid name.` Write only the name: `createElement("li")`.

**3. Adding lines with innerHTML +=.**

```js
slip.innerHTML += `<li>${item.name}</li>`;
```

Every old line is rebuilt, so any button handlers on them stop working. A name with tags turns into HTML. Use `createElement`, `textContent` and `append`.

**4. Removing something that is not there.**

```js
slip.querySelector('[data-code="BTY"]').remove();
```

If there are no booties on the slip, the console shows `TypeError: Cannot read properties of null (reading 'remove')`. Store the result and check it against `null` first.

**5. Expecting append to copy.**

```js
saved.append(line);
slip.append(line);
```

There is still only one `line`. The second `append` moves it from `saved` to `slip`. Create a second element if you need two.
