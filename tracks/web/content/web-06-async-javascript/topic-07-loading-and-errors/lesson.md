## Hook

You walk up to the Aundh shop and the shutter is half down. Is it closed? Is someone inside sorting stock? Should you wait or leave? With no sign, you guess, and you probably leave.

A blank web page is that half-closed shutter. The customer can't tell "still loading" from "broken" from "nothing to show".

Every request your page makes needs a sign for each thing that can happen. In this ticket you put up those signs.

## Concept

### Every request goes through states

```callout jargon
A **UI state** is one of the few situations a part of the page can be in. For a request, the usual states are **loading** (asked, waiting), **ready** (the answer arrived), **empty** (the answer arrived but has nothing in it) and **error** (it failed).
```

A request always starts in loading and ends in exactly one of the others. Your job is to show something for every state, so the page is never blank.

```animate
title: The catalogue's status line
kind: scene
nodes:
  - { id: page, label: Status line, emoji: "🪧", value: "(blank)" }
  - { id: code, label: Your code, emoji: "🧑‍💻", value: idle }
  - { id: server, label: dhaaga.in API, emoji: "🖥️", value: idle }
steps:
  - say: The customer opens the catalogue. Before anything else, your code shows the loading message.
    set: { page: "Loading the catalogue...", code: "loading" }
  - say: Then it asks the server. On 3G the answer takes eight seconds, but the customer can see something is happening.
    send: { from: code, to: server, label: "GET /api/catalogue" }
    set: { server: busy }
  - say: The server answers with three items. Your code draws them and moves to the ready state.
    send: { from: server, to: code, label: "200 [3 items]", back: true }
    set: { code: ready, server: idle, page: "(3 items shown)" }
  - say: Another day, the connection drops. fetch rejects, and your catch block moves to the error state.
    set: { code: error, page: "Couldn't load the catalogue..." }
  - say: A Try again button appears. One tap, and the whole trip starts again from loading.
    set: { code: loading, page: "Loading the catalogue..." }
```

### Show loading straight away

An `async` function runs normally until its first `await`. So set the loading state **before** you call `fetch`, and the customer sees it at once.

```js
async function loadCatalogue(status) {
  status.textContent = "Loading the catalogue...";
  status.dataset.state = "loading";
  const response = await fetch("/api/catalogue");
  // ...
}
```

If you set it after the `await`, it appears only once the waiting is over, which is too late to help.

### Syntax: try, catch and finally for a request

```js
try {
  // ask, check, and show the answer
} catch (error) {
  // show the error state
} finally {
  // undo the loading things, either way
}
```

- `try` holds the happy path: fetch, check `ok`, read the body, show it.
- `catch` runs if anything in `try` throws. It shows the error state.
- `finally` runs after either. It is the place to re-enable a button or hide a spinner.

### Two kinds of failure, one catch

A request can fail in two different ways:

- **Network failure:** no answer at all, such as flight mode. `fetch` rejects, often with `TypeError: Failed to fetch`.
- **HTTP failure:** an answer that isn't ok, such as `500`. `fetch` succeeds, so nothing throws on its own.

Throw for the second kind yourself. Then both land in the same `catch`, and one piece of code handles every failure.

```js
async function getCatalogue() {
  const response = await fetch("/api/catalogue");
  if (!response.ok) {
    throw new Error(`Catalogue failed: ${response.status}`);
  }
  return response.json();
}
```

Here are both paths with a pretend `fetch`, so you can run them:

```js run
async function pretendFetch(works) {
  if (!works) throw new TypeError("Failed to fetch");
  return { ok: true, status: 200, json: async () => [{ name: "Golu the elephant" }] };
}

async function load(works) {
  console.log("state: loading");
  try {
    const response = await pretendFetch(works);
    const items = await response.json();
    console.log(`state: ready (${items.length} item)`);
  } catch (error) {
    console.log(`state: error (${error.message})`);
  } finally {
    console.log("button enabled again");
  }
}

async function main() {
  await load(true);
  await load(false);
}

main();
```

### Friendly error messages

`TypeError: Failed to fetch` is for developers. A customer needs two things: what happened, and what to do next.

- Bad: `Error: Catalogue failed: 500`
- Good: `Couldn't load the catalogue. Check your connection and try again.`

Put the technical details in `console.error(error)` for yourself, and the friendly sentence on the page. Add a **Try again** button, so the customer doesn't have to reload the whole page.

### Empty is not an error

A shop with no tote bags in stock isn't broken. If the server answers `200` with an empty list `[]`, the request worked. Show a kind sentence, such as `No items yet. Check back soon!`, not an error, and not a blank space.

### Disable the button while loading

If a customer taps **Check** five times on a slow phone, five requests go out. Disable the button when the request starts, and enable it again in `finally`.

```js
async function checkPin(button, output, pin) {
  button.disabled = true;
  try {
    const response = await fetch(`/api/delivery?pin=${pin}`);
    const data = await response.json();
    output.textContent = data.deliverable ? "We deliver here" : "Not yet";
  } finally {
    button.disabled = false;
  }
}
```

### Styling each state with data-state

```callout jargon
A **data attribute** is your own attribute whose name starts with `data-`, such as `data-state="loading"`. In JavaScript you read and write it through `element.dataset.state`.
```

Setting `status.dataset.state = "error"` writes `data-state="error"` into the HTML. A designer can then style each state in CSS:

```css
.status[data-state="loading"] { color: #2E3A8C; }
.status[data-state="error"] { color: #B00020; }
```

## Worked example

The PIN box at checkout checks delivery. Here it is with every state:

```js
async function showDelivery(button, output, pin) {
  button.disabled = true;
  output.textContent = "Checking your PIN...";
  output.dataset.state = "loading";
  try {
    const response = await fetch(`/api/delivery?pin=${pin}`);
    if (!response.ok) {
      throw new Error(`Delivery check failed: ${response.status}`);
    }
    const data = await response.json();
    output.textContent = data.deliverable ? `Delivery in ${data.days} days` : "We don't deliver here yet";
    output.dataset.state = "ready";
  } catch (error) {
    console.error(error);
    output.textContent = "Couldn't check your PIN. Please try again.";
    output.dataset.state = "error";
  } finally {
    button.disabled = false;
  }
}
```

Line by line:

1. The first three lines run at once: the button greys out, and the loading text appears.
2. `try` asks, checks `ok`, reads the body and shows the answer, moving to the ready state.
3. Any failure, network or HTTP, jumps to `catch`. It logs the details for developers and shows a friendly sentence.
4. `finally` enables the button again, whether the check worked or failed.

## Try it

In the runnable example above, add a third call to `main`: `await load(true)` again. Then change `pretendFetch` so that a working call returns an empty list `[]`, and print `state: empty` when `items.length` is 0.

## Recap

- Every request is in one UI state: loading, then ready, empty or error. Show something for each one.
- Set the loading state before the first `await`, so it appears at once.
- Throw when `response.ok` is false, so HTTP failures and network failures share one `catch`.
- Show friendly error text with a way to try again. Keep technical details in the console.
- Use `finally` to undo loading things, such as a disabled button. Mark the state with `data-state` for styling.

## Common mistakes

**1. Setting the loading message after the await.**

```js
async function loadCatalogue(status) {
  const response = await fetch("/api/catalogue");
  status.textContent = "Loading the catalogue...";
}
```

The page stays blank for the whole wait, then flashes "Loading" when the answer has already arrived. Fix: set the loading state on the first line, before `fetch`.

**2. Leaving the button disabled after a failure.**

```js
async function check(button) {
  button.disabled = true;
  const response = await fetch("/api/delivery?pin=411001");
  button.disabled = false;
}
```

If `fetch` rejects, the last line never runs, and the button stays grey forever. Fix: put `button.disabled = false;` in a `finally` block.

**3. Showing the raw error to customers.**

```js
function showFailure(status, error) {
  status.textContent = error.message;
}
```

The customer reads `Failed to fetch` or `Catalogue failed: 500`, which means nothing to them. Fix: show a sentence that says what happened and what to do, and log `error` with `console.error`.

**4. Treating an empty list as nothing to do.**

```js
function draw(list, items) {
  for (const item of items) {
    const li = document.createElement("li");
    li.textContent = item.name;
    list.append(li);
  }
}
```

With `[]`, nothing is drawn, and the page looks broken. Fix: check `items.length === 0` and show an empty-state sentence instead.
