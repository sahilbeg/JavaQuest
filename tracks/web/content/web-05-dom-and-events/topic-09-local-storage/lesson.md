## Hook

Close your eyes and count to ten. Raju can still tell you what Mrs. Rao bought last week, because he wrote it in the stall's notebook. His memory of the morning rush is long gone, but the notebook isn't.

A web page has only the first kind of memory. Every variable lives while the page is open. Reload, close the tab or switch off the phone, and it is all gone. This ticket gives the page a notebook.

## Concept

### Why variables forget

When a page loads, its JavaScript starts from nothing. Variables are made as the code runs, and they vanish when the page goes away.

```callout jargon
**localStorage** is a small store of text that the browser keeps for each website, on that device. What you save there is still there after a reload, or after the browser is closed and opened again.
```

```animate
title: What survives a reload
kind: scene
nodes:
  - { id: page, label: The page, emoji: "📄", row: 0, col: 0 }
  - { id: memory, label: JavaScript variables, emoji: "🧠", value: "pin = ''", row: 1, col: 0 }
  - { id: storage, label: localStorage, emoji: "📓", value: empty, row: 1, col: 3 }
  - { id: customer, label: Mrs. Rao, emoji: "👵", row: 0, col: 3 }
steps:
  - say: Mrs. Rao types her PIN code, 411038, on the delivery check.
    send: { from: customer, to: page, label: "411038" }
  - say: The code keeps it in a variable. That lives only while this page is open.
    send: { from: page, to: memory, label: "pin = '411038'" }
    set: { memory: "pin = '411038'" }
  - say: The code also writes it into localStorage, under the key dhaaga:pin.
    send: { from: memory, to: storage, label: "setItem('dhaaga:pin', ...)" }
    set: { storage: "dhaaga:pin = 411038" }
  - say: She closes the tab. Every variable is thrown away. The browser keeps localStorage.
    set: { memory: "(gone)" }
    focus: [memory, storage]
  - say: Next week the page loads again, with fresh, empty variables.
    set: { memory: "pin = ''" }
  - say: The first thing the code does is read the notebook. Her PIN is back, and she types nothing.
    send: { from: storage, to: memory, label: "getItem('dhaaga:pin')", back: true }
    set: { memory: "pin = '411038'" }
```

### Syntax: setItem and getItem

```js
localStorage.setItem("key", "text");
const text = localStorage.getItem("key");
```

- `setItem` saves the text under a name, called the **key**. Saving again under the same key replaces the old text.
- `getItem` returns the saved text, or `null` when nothing was ever saved under that key.
- Each website has its own store. dhaaga.in can't read another site's localStorage.

```callout jargon
A **key** is the name a value is saved under, like a label on a drawer. To read the value back, you ask for the same key.
```

### Only strings

localStorage stores text and nothing else. Anything you give it is turned into a string first:

```js
localStorage.setItem("count", 3);                  // saved as "3"
localStorage.setItem("ids", ["DC-1042", "DC-1013"]); // saved as "DC-1042,DC-1013"
localStorage.setItem("item", { id: "DC-1042" });     // saved as "[object Object]"
```

The number comes back as the string `"3"`. The object comes back as useless text. To save arrays and objects properly, turn them into JSON text first.

### Syntax: JSON.stringify and JSON.parse

You met JSON with the catalogue in the last module. It is the tool for this job:

```js
const ids = ["DC-1042", "DC-1013"];
localStorage.setItem("dhaaga:wishlist", JSON.stringify(ids));
const saved = JSON.parse(localStorage.getItem("dhaaga:wishlist"));
```

- `JSON.stringify(value)` turns an array or object into a string that keeps its shape.
- `JSON.parse(text)` turns that string back into a real array or object.

Watch the round trip:

```animate
title: An array goes in as text and comes back as an array
kind: code
language: js
code: |
  const wishlist = ["DC-1042", "DC-1013"];
  const text = JSON.stringify(wishlist);
  console.log(text);
  console.log(typeof text);
  const back = JSON.parse(text);
  console.log(back.length);
  console.log(String({ id: "DC-1042" }));
steps:
  - line: 1
    objects: { w1: { type: Array, fields: { "0": '"DC-1042"', "1": '"DC-1013"' } } }
    vars: { wishlist: "@w1" }
    say: The wishlist is an array of two item ids.
  - line: 2
    vars: { text: "'[\"DC-1042\",\"DC-1013\"]'" }
    say: JSON.stringify writes the whole array out as one string, brackets and quotes included.
  - line: 3
    out: '["DC-1042","DC-1013"]'
    say: That string is what localStorage would keep.
  - line: 4
    out: "string"
    say: It really is just text now.
  - line: 5
    objects: { w2: { type: Array, fields: { "0": '"DC-1042"', "1": '"DC-1013"' } } }
    vars: { back: "@w2" }
    say: JSON.parse reads the text and builds a brand-new array with the same items.
  - line: 6
    out: "2"
    say: It is a real array again, with a length of 2.
  - line: 7
    out: "[object Object]"
    say: Without JSON, an object turns into this useless text. That is what setItem would save.
```

### Nothing saved yet

The first time someone visits, nothing is saved. `getItem` returns `null`, so give a sensible default:

```js
const pin = localStorage.getItem("dhaaga:pin") ?? "";
```

`??` uses the right side only when the left side is `null` or `undefined`. A saved empty string stays empty.

### When the saved text is broken

Saved text can be broken: an old version of the site saved something else, a browser extension changed it, or someone edited it in DevTools. Then `JSON.parse` throws an error, and your page stops.

```js run crash
JSON.parse("[DC-1042");
```

Guard it with `try` and `catch`, from the errors ticket, and check the shape too:

```js run
function readIds(text) {
  if (text === null) {
    return [];
  }
  try {
    const value = JSON.parse(text);
    return Array.isArray(value) ? value : [];
  } catch {
    return [];
  }
}

console.log(readIds(null));
console.log(readIds('["DC-1042"]'));
console.log(readIds("[DC-1042"));
console.log(readIds('{"DC-1042":true}'));
```

It prints `[]`, `[ 'DC-1042' ]`, `[]` and `[]`. Only good text gives a list. Everything else gives a safe empty one.

```callout jargon
A **safe default** is the value your code falls back to when the real value is missing or broken, so the page keeps working. For a list, that is usually an empty array.
```

### Syntax: removeItem and clear

```js
localStorage.removeItem("dhaaga:pin");   // forget one key
localStorage.clear();                    // forget every key this site saved
```

`clear` wipes everything the whole site saved, not just yours. On a big site, other features may keep things there too, so prefer `removeItem` for each key you own.

### Name your keys

One website shares one store across all its pages. Give your keys a prefix, like `dhaaga:pin` and `dhaaga:wishlist`, so they can't collide with another feature's keys. Keep each key in a constant, so saving and loading always use exactly the same name.

### What not to keep there

- **Nothing secret.** Any script on the page can read localStorage. No passwords, card numbers or login tokens.
- **Only this device.** Mrs. Rao's laptop doesn't know what her phone saved.
- **It can vanish.** People clear their browser data. Treat saved values as a convenience, never as the only copy.
- **It is small:** a few megabytes per site. Fine for a PIN and a wishlist, not for photos.

```callout tip
Open DevTools, then the **Application** panel, then **Local Storage**. You can see, edit and delete every key the page saved. It is the quickest way to test a broken value.
```

## Worked example

The catalogue remembers which colour filter Mrs. Rao picked last.

```js
const COLOUR_KEY = "dhaaga:colour";
const select = document.querySelector("#colour");

function loadColour() {
  return localStorage.getItem(COLOUR_KEY) ?? "All";
}

select.value = loadColour();

select.addEventListener("change", () => {
  localStorage.setItem(COLOUR_KEY, select.value);
});
```

1. The key lives in one constant, so the save and the load can't drift apart.
2. On load, `getItem` gives the saved colour, or `null` on a first visit. `??` turns that into `"All"`.
3. Each time she picks a colour, the `change` handler saves it at once.
4. A colour is already a string, so no JSON is needed here. A list or an object would need `JSON.stringify`.

## Try it

Node has no localStorage, so this stand-in uses a `Map` with the same two methods. Run it, then save a PIN and read it back.

```js run
const store = new Map();

function setItem(key, value) {
  store.set(key, String(value));
}

function getItem(key) {
  return store.has(key) ? store.get(key) : null;
}

setItem("dhaaga:wishlist", JSON.stringify(["DC-1042", "DC-1013"]));
console.log(getItem("dhaaga:wishlist"));
console.log(JSON.parse(getItem("dhaaga:wishlist")).length);
console.log(getItem("dhaaga:pin"));
```

## Recap

- Variables vanish on reload. **localStorage** keeps text for a site, on that device, until it is removed.
- `setItem(key, text)` saves, `getItem(key)` reads (or gives `null`), `removeItem(key)` forgets.
- Everything is stored as a string. Use `JSON.stringify` to save arrays and objects, and `JSON.parse` to read them.
- Always plan for missing or broken values: `?? default`, and `try`/`catch` with a shape check around `JSON.parse`.
- Prefix your keys, keep them in constants, and never store secrets.

## Common mistakes

**1. Saving an array or object without JSON.**

```js
localStorage.setItem("dhaaga:wishlist", ids);
```

The array is saved as `DC-1042,DC-1013`, and an object as `[object Object]`. `JSON.parse` on that text throws. Save `JSON.stringify(ids)`.

**2. Forgetting to parse.**

```js
const ids = localStorage.getItem("dhaaga:wishlist");
console.log(ids.length);   // 21, not 2
```

`ids` is still the text `["DC-1042","DC-1013"]`, so `length` counts its characters. Use `JSON.parse(...)`.

**3. Parsing nothing.**

```js
const ids = JSON.parse(localStorage.getItem("dhaaga:wishlist"));
ids.includes("DC-1042");
```

On a first visit, `JSON.parse(null)` gives `null`, and the next line shows `TypeError: Cannot read properties of null (reading 'includes')`. Check for `null` first, and return `[]`.

**4. Trusting the saved text.**

```js
return JSON.parse(localStorage.getItem("dhaaga:wishlist")) ?? [];
```

One broken value, and the page throws a `SyntaxError` on every visit, until the customer clears their browser data. Wrap the parse in `try`/`catch` and fall back to `[]`.

**5. Clearing everything.**

```js
localStorage.clear();
```

"Forget my PIN" also wiped the theme, the recently viewed list and anything else the site saved. Remove only your own keys with `removeItem`.
