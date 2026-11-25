## Hook

Picture Raju restocking the shelf at the Aundh shop. He could carry one coaster set at a time from the storeroom: thirty trips, and the shelf looks half-empty the whole time. Or he could fill a tray in the storeroom and carry it out once.

Your page works the same way. Each change to the live page is a trip. Each time your code asks "how tall is this card?", the browser may have to stop and rearrange the whole shelf first. Fast pages make few trips, ask their questions before they start moving things, and redraw at most once per screen refresh.

## Concept

### What the browser does after your code runs

Your JavaScript changes the page. Then the browser turns those changes into pixels, in steps.

```callout jargon
**Layout** (also called **reflow**) is when the browser works out the size and position of every box. **Paint** is when it fills in the pixels. A **frame** is one picture on the screen. Most screens show 60 frames a second, so one frame lasts about 16 milliseconds.
```

The browser is lazy in a good way. It doesn't lay out after every single change. It waits until your code finishes, then does style, layout and paint once, before the next frame.

```animate
title: From your code to the screen
kind: scene
nodes:
  - { id: js, label: Your JavaScript, emoji: "📜", value: running, row: 0, col: 0 }
  - { id: layout, label: Layout, emoji: "📐", value: waiting, row: 0, col: 2 }
  - { id: paint, label: Paint, emoji: "🎨", value: waiting, row: 0, col: 4 }
  - { id: screen, label: Screen, emoji: "📱", value: "frame 1", row: 2, col: 4 }
steps:
  - say: The customer types a letter. Your input handler runs and changes the list.
    focus: [js]
  - say: The browser notes that layout is now out of date, but doesn't redo it yet.
    set: { js: done, layout: "out of date" }
  - say: Your code is finished. Now the browser lays out every box once.
    send: { from: js, to: layout, label: "changes" }
    set: { layout: done }
  - say: Paint fills in the pixels for the boxes that changed.
    send: { from: layout, to: paint, label: "boxes" }
    set: { paint: done }
  - say: The new frame reaches the screen. All of this has to fit in about 16 ms, or the page stutters.
    send: { from: paint, to: screen, label: "pixels" }
    set: { screen: "frame 2" }
    focus: [screen]
```

When the work doesn't fit in a frame, the screen keeps showing the old picture. The customer sees a stutter, which people call **jank**.

### Syntax: DocumentFragment

```js
const fragment = document.createDocumentFragment();
fragment.append(document.createElement("li"));
document.querySelector("#products").replaceChildren(fragment);
```

```callout jargon
A **DocumentFragment** is a lightweight box for elements that isn't part of the page. You build inside it freely. When you insert the fragment, its children move into the page, and the empty box is left behind.
```

`replaceChildren(fragment)` removes the old children and puts the new ones in, as a single change to the page.

### Why one change beats three hundred

You built lists from data in [Render functions](/t/vanilla-08-components-without-a-framework/topic-01-render-functions). Usually you appended each item straight into the page:

```js
function renderSlow(list, items) {
  list.innerHTML = "";
  for (const item of items) {
    list.appendChild(item);
  }
}
```

Each `appendChild` here changes the **live** page. Every live change makes the browser update its records, tell any observers, and mark styles as out of date. With 300 cards on a slow phone, that adds up.

Building into a fragment first means the page changes once. The customer also never sees a half-built list, because the list goes from old to new in one step.

```callout tip
Watch out for list.innerHTML += "<li>...</li>" inside a loop. Each time, the browser turns the whole list back into text, adds a bit, and builds every card again from scratch.
```

### Layout thrashing: reads mixed with writes

Some properties **read** the layout: `offsetHeight`, `offsetWidth`, `getBoundingClientRect()`, `scrollTop`. To answer them truthfully, the browser must lay out the page right now if anything changed.

```callout jargon
A **forced layout** happens when your code reads a size after changing the page, so the browser must lay out at once instead of waiting. **Layout thrashing** is doing that over and over in a loop: write, read, write, read.
```

This program pretends to be a browser. A write marks the layout dirty, and a read lays out only if it's dirty.

```animate
title: Mixed reads and writes versus batched ones
kind: code
language: js
code: |
  let dirty = false;
  let layouts = 0;
  const write = () => { dirty = true; };
  const read = () => { if (dirty) { layouts += 1; dirty = false; } };
  for (let i = 0; i < 3; i++) { write(); read(); }
  console.log("mixed:", layouts);
  layouts = 0;
  for (let i = 0; i < 3; i++) write();
  for (let i = 0; i < 3; i++) read();
  console.log("batched:", layouts);
steps:
  - line: 1
    vars: { dirty: "false" }
    say: The pretend browser starts with an up-to-date layout.
  - line: 2
    vars: { layouts: 0 }
    say: layouts counts how many times it had to stop and lay out.
  - line: 5
    vars: { layouts: 3, dirty: "false" }
    say: Write then read, three times. Every read finds dirty layout, so it lays out three times.
  - line: 6
    out: "mixed: 3"
    say: Three forced layouts. With 300 cards, it would be 300.
  - line: 8
    vars: { layouts: 0, dirty: "true" }
    say: Now all the writes come first. The layout is dirty, but nobody has asked yet.
  - line: 9
    vars: { layouts: 1, dirty: "false" }
    say: The first read lays out once. The other reads find it already fresh.
  - line: 10
    out: "batched: 1"
    say: Same work, one layout. The rule is simple. Do all your reads, then all your writes.
```

### Syntax: requestAnimationFrame

```js
requestAnimationFrame((time) => {
  console.log("drawing just before frame at", time);
});
```

`requestAnimationFrame(callback)` asks the browser to run `callback` once, just before it paints the next frame. The callback gets a timestamp in milliseconds.

```callout jargon
**requestAnimationFrame** (people say "rAF") is the browser's "call me right before you draw" hook. It runs at the screen's speed, usually 60 times a second, and pauses when the tab is hidden.
```

Code that changes what the customer sees belongs here. It runs at the perfect moment: after all the events of this frame, and before the pixels.

### One draw per frame

A fast typist fires several `input` events within one frame. Redrawing the list for each of them is wasted work, because only the last drawing ever reaches the screen.

The fix is to **coalesce**: remember the newest state, and ask for a frame only if you haven't already.

```js
let latest = null;
let frameAsked = false;

function scheduleDraw(state, draw) {
  latest = state;
  if (frameAsked) {
    return;
  }
  frameAsked = true;
  requestAnimationFrame(() => {
    frameAsked = false;
    draw(latest);
  });
}
```

```callout jargon
To **coalesce** updates is to merge many requests into one. Ten calls in one frame lead to a single draw, with the newest state.
```

This is not the same as the debounce from [Debounce](/t/web-06-async-javascript/topic-10-debounce). A debounce waits for the customer to **stop** typing. Coalescing keeps up while they type, but never draws more than the screen can show.

### Measure first

Guessing where a page is slow is usually wrong. Measure before you change anything, and again after.

- **The Performance panel** in DevTools records a few seconds and shows every long task, layout and paint. Purple "Layout" bars with a red corner are forced layouts.
- **CPU throttling** in the same panel makes your laptop act like Raju's old phone.
- **`performance.now()`** gives a precise timestamp, for timing one piece of code.

```js
const start = performance.now();
renderSlow(document.querySelector("#products"), []);
console.log(`redraw took ${(performance.now() - start).toFixed(1)} ms`);
```

```callout jargon
A **long task** is any piece of work that keeps the browser busy for more than 50 ms. During a long task, taps and key presses wait, so the page feels stuck.
```

## Worked example

Here is the catalogue's search, before and after. The page has an `<input id="search">` and a `<ul id="products">`. Before:

```js
function onSearchSlow(list, products, text) {
  list.innerHTML = "";
  for (const product of products) {
    if (product.name.toLowerCase().includes(text)) {
      const li = document.createElement("li");
      li.textContent = product.name;
      list.appendChild(li);
      li.style.height = "";
      li.style.minHeight = `${li.offsetHeight}px`;
    }
  }
}
```

Three problems hide in eight lines:

- every card goes into the live page on its own;
- reading `li.offsetHeight` right after a write forces a layout per card;
- `onSearchSlow` runs on every `input` event, even several per frame.

After:

```js
function drawList(list, products, text) {
  const fragment = document.createDocumentFragment();
  const cards = [];
  for (const product of products) {
    if (product.name.toLowerCase().includes(text)) {
      const li = document.createElement("li");
      li.textContent = product.name;
      fragment.append(li);
      cards.push(li);
    }
  }
  list.replaceChildren(fragment);
  const heights = cards.map((card) => card.offsetHeight);
  cards.forEach((card, index) => {
    card.style.minHeight = `${heights[index]}px`;
  });
}
```

Line by line:

- The cards are built in a fragment, then `replaceChildren` swaps the whole list in one change.
- All the reads (`offsetHeight`) happen in one pass, so the browser lays out once.
- All the writes come after, in a second pass. They cause one more layout, at the next frame.

The input handler then calls the one-draw-per-frame scheduler with the newest search text, instead of drawing straight away.

## Try it

This runs a pretend frame queue, so you can see coalescing without a browser. Run it.

```js run
const frames = [];
const fakeFrame = (callback) => frames.push(callback);
const draws = [];
let latest = "";
let waiting = false;

function schedule(text) {
  latest = text;
  if (waiting) {
    return;
  }
  waiting = true;
  fakeFrame(() => {
    waiting = false;
    draws.push(latest);
  });
}

for (const text of ["g", "go", "gol", "golu"]) {
  schedule(text);
}
console.log("frames asked for:", frames.length);
frames.shift()();
console.log("drawn:", draws.join(", "));
```

It prints:

```text
frames asked for: 1
drawn: golu
```

Four letters, one frame, one draw, with the newest text.

1. After `frames.shift()()`, call `schedule("golu ")` and run the next frame. Does a second draw happen? Why?
2. Delete the `if (waiting)` check. How many frames are asked for now, and what does `draws` hold?

## Recap

- After your code runs, the browser does layout and paint before the next frame. Everything must fit in about 16 ms, or the page stutters.
- Build many elements in a `DocumentFragment`, then insert them with one `replaceChildren`, so the live page changes once.
- Reading sizes like `offsetHeight` after a change forces a layout. Do all the reads first, then all the writes.
- Put drawing code in `requestAnimationFrame`, and coalesce: remember the newest state and draw at most once per frame.
- Measure with the Performance panel and CPU throttling before and after you optimise.

## Common mistakes

**1. Building a list with innerHTML +=.**

```js
for (const product of [{ name: "Golu the elephant" }]) {
  document.querySelector("#products").innerHTML += `<li>${product.name}</li>`;
}
```

Symptom: the list gets slower to draw the longer it is, and any listeners on old cards vanish. Each `+=` turns the list into text and rebuilds every card. It's also an XSS risk with customer text. Fix: build elements in a fragment and insert once.

**2. Reading a size inside the loop that writes.**

```js
for (const card of document.querySelectorAll(".product")) {
  card.style.height = "";
  card.style.height = `${card.offsetHeight + 8}px`;
}
```

Symptom: the Performance panel shows one purple Layout bar per card, each marked as forced. Fix: one loop that clears, one loop that reads into an array, one loop that writes.

**3. Asking for a frame on every event.**

```js
document.querySelector("#search").addEventListener("input", (event) => {
  requestAnimationFrame(() => console.log("draw for", event.target.value));
});
```

Symptom: four quick letters queue four callbacks, and all four draw in the same frame. Fix: keep a "frame already asked" flag, and draw the newest state once.

**4. Using setTimeout(draw, 16) as a frame.**

```js
setTimeout(() => console.log("draw"), 16);
```

Symptom: drawing drifts out of step with the screen, sometimes missing a frame and sometimes drawing twice. It also keeps running in hidden tabs. Fix: `requestAnimationFrame` runs exactly once per frame, and pauses when nobody is looking.

**5. Optimising without measuring.**

```js
// "This feels slow, let's cache everything"
const cache = new Map();
```

Symptom: the code gets harder to read, and the stutter is still there, because the slow part was somewhere else. Fix: record with the Performance panel first, find the long task, fix that, and record again.
