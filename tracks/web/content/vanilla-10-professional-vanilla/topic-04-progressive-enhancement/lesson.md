## Hook

Pune station has an escalator, and right next to it, a staircase. The escalator is nicer. But when it stops for repairs, nobody is stuck: they take the stairs.

A good web page is built the same way. Plain HTML is the staircase: links that go somewhere and forms that send something. JavaScript is the escalator on top: smoother, quicker, nicer. When the script is late, broken or blocked, the customer still gets up the stairs, and Dhaaga still gets the order.

## Concept

### What progressive enhancement means

```callout jargon
**Progressive enhancement** means building a page in layers. First HTML that works on its own, then CSS that makes it look good, then JavaScript that makes it nicer. Each layer improves the one below, and none of them is needed for the basics to work.
```

| Layer | Its job | If it's missing |
|---|---|---|
| HTML | Content, links, forms that send to the server | Nothing works: this layer must always be there |
| CSS | Layout, colours, Sana's brand | The page looks plain, but every link and form still works |
| JavaScript | No page reloads, instant filters, live counts | The page reloads more, but the customer can still shop |

The opposite approach starts from the script: an empty page that JavaScript fills in. The single-page app you built in [What a single-page app is](/t/vanilla-09-single-page-apps/topic-01-what-is-an-spa) works that way. That's fine for some screens, but the shop page is where money comes in, so it gets the staircase.

### When the script doesn't run

It's tempting to think "everyone has JavaScript". Everyone does, but not always **yet**, and not always **working**:

- **It hasn't arrived.** On slow mobile data, the HTML shows first. The script may take seconds more, and customers tap in the meantime.
- **It crashed.** One error in one file, maybe from an old browser, can stop everything after it.
- **It was blocked.** Office firewalls, data-saver modes and browser extensions sometimes block scripts.
- **The reader isn't a browser.** Link-preview bots on WhatsApp and many search crawlers read the HTML without running much script.

```animate
title: A customer taps before the script arrives
kind: scene
nodes:
  - { id: phone, label: Customer on a train, emoji: "📱", value: "blank", row: 0, col: 0 }
  - { id: server, label: dhaaga.in, emoji: "🗄️", row: 0, col: 4 }
  - { id: js, label: main.js, emoji: "📜", value: "on its way", row: 2, col: 2 }
steps:
  - say: The phone asks for the shop page. The HTML is small, so it arrives in a second.
    send: [{ from: phone, to: server, label: "GET /shop" }, { from: server, to: phone, label: "HTML", back: true }]
    set: { phone: "shop page shown" }
  - say: The script is much bigger, and still crawling over the slow connection.
    focus: [js]
  - say: The customer taps Add to cart. With a real form, the browser posts it to the server by itself.
    send: { from: phone, to: server, label: "POST /cart id=DC-1042" }
  - say: The server adds Golu and sends back the page with Cart (1). It's one reload, but the order is safe.
    send: { from: server, to: phone, label: "page, Cart (1)", back: true }
    set: { phone: "Cart (1)" }
  - say: Now the script arrives. From here on, Add to cart works without a reload.
    send: { from: js, to: phone, label: "enhance()" }
    set: { js: "running", phone: "enhanced" }
```

### Syntax: a form that works without JavaScript

```html
<form action="/search" method="get">
  <label for="q">Search</label>
  <input id="q" name="q" type="search">
  <button type="submit">Search</button>
</form>
```

You met forms in [Forms](/t/web-01-html/topic-07-forms). Here is what the browser does with them on its own, no script needed:

- **`method="get"`** puts the fields in the address: `/search?q=golu`. Use it for things that only **read**, like searching. The result can be bookmarked and shared.
- **`method="post"`** sends the fields in the request body. Use it for things that **change** something, like adding to a cart or placing an order.
- **`name`** is what each field is called when it's sent. A field without a `name` isn't sent at all.
- **`<input type="hidden">`** sends a value the customer doesn't see, such as which item the Add button belongs to.

The server must understand these requests too. That's the deal: the same `/search` and `/cart` addresses work for the plain form and for the script.

### Syntax: feature detection

```js
if ("IntersectionObserver" in window) {
  console.log("lazy-load the photos");
} else {
  console.log("load every photo now");
}
```

```callout jargon
**Feature detection** checks whether the browser has a feature before using it, and falls back if it doesn't. It's the opposite of **browser sniffing**, which guesses from the browser's name. Sniffing goes wrong every time a browser changes.
```

Some features have no fallback to write: the page simply works the plain way. If there's no `fetch`, don't enhance the forms. They keep working with reloads.

CSS has its own version, `@supports (display: grid) { ... }`, which applies styles only where the browser understands them.

### Enhancing a form

To enhance a form, listen for its `submit` event, stop the normal page load with `event.preventDefault()`, and send the same data with `fetch`.

```js
const notifyForm = document.querySelector("#notify-form");
notifyForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const data = new FormData(notifyForm);
  const response = await fetch(notifyForm.action, { method: "POST", body: data });
  console.log(response.ok ? "We'll tell you" : "Please try again");
});
```

```callout jargon
**FormData** collects a form's fields, by their `name`, exactly as the browser would send them. `new FormData(form)` includes hidden inputs, and `fetch` can send it as the body.
```

Three details make it professional:

- **Listen for `submit`, not `click`.** Submit also fires when the customer presses Enter in a field.
- **Disable the button while sending.** Otherwise an impatient double-tap orders two Golus.
- **Say when it fails.** A failed request must show a message. Silently doing nothing is worse than the reload you replaced.

### Keeping the address in step

When the script filters the shop in place, the address bar would still say `/shop`. Refresh, and the search is gone. Share the link, and your friend sees everything.

The plain form put the search in the address: `/search?q=golu`. The enhanced version should too, without a reload:

```js
history.replaceState(null, "", "?q=golu");
```

You used `history` for routing in [Routing with the History API](/t/vanilla-09-single-page-apps/topic-03-history-routing). `replaceState` changes the address without adding a Back step. A search refined five times shouldn't need five presses of Back.

## Worked example

Golu is sold out, so his page has a "Tell me when he's back" form. First, the plain HTML, which works on its own:

```html
<form id="notify-form" action="/notify" method="post">
  <input type="hidden" name="id" value="DC-1042">
  <label for="phone">WhatsApp number</label>
  <input id="phone" name="phone" type="tel">
  <button type="submit">Tell me when he's back</button>
  <p id="notify-status" role="status"></p>
</form>
```

Then the script, which only makes it nicer:

```js
function enhanceNotify(form, fetchFn) {
  if (typeof fetchFn !== "function") {
    return;
  }
  const button = form.querySelector("button");
  const status = form.querySelector("#notify-status");
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    button.disabled = true;
    try {
      const response = await fetchFn(form.getAttribute("action"), { method: "POST", body: new FormData(form) });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      status.textContent = "Done! We'll send you a WhatsApp message.";
    } catch {
      status.textContent = "Sorry, that didn't work. Please try again.";
    } finally {
      button.disabled = false;
    }
  });
}
```

Line by line:

- Without `fetch`, the function returns at once. The form keeps posting the old way.
- `preventDefault()` runs first, before any `await`. After an `await`, it would be too late: the browser would already be loading `/notify`.
- The script posts to the **same** address as the plain form, with the same fields. The server needs no second version.
- `finally` re-enables the button whether it worked or not, and the live region from [Accessibility in depth](/t/vanilla-10-professional-vanilla/topic-02-accessibility-in-depth) announces the result.

## Try it

A `get` form turns its fields into the address. `URLSearchParams` does the same thing, in both directions. Run it.

```js run
const fields = [["q", "coaster set"], ["colour", "Indigo"]];
const query = new URLSearchParams(fields).toString();
console.log("/search?" + query);

const back = new URLSearchParams(query);
console.log(back.get("q"), "|", back.get("colour"));
```

It prints:

```text
/search?q=coaster+set&colour=Indigo
coaster set | Indigo
```

The space became `+`, which is how forms write spaces in an address. Reading it back gives the space again.

1. Add a field `["max", "₹500"]`. How is the `₹` written in the address?
2. Make the search `"Rani pink & mint"`. Why must the `&` be encoded, and what does it become?

## Recap

- Build in layers: HTML that works alone, CSS on top, then JavaScript that improves it. The basics never depend on the script.
- Scripts are often late, broken, blocked or not run at all, so links and forms must work without them.
- Use real links and real forms: `get` for searches, `post` for changes, `name` on every field, hidden inputs for ids.
- Check for features before using them, and enhance forms by catching `submit`, calling `preventDefault()`, and posting the same data with `fetch`. Disable the button while sending, and say when it fails.
- Keep the address in step with `history.replaceState`, so searches survive a refresh and can be shared.

## Common mistakes

**1. A button that only works with the script.**

```html
<button class="add" type="button" data-id="DC-1042">Add to cart</button>
```

Symptom: until `main.js` arrives, the button does nothing at all. On a slow train, that's the whole visit. Fix: a `<form action="/cart" method="post">` with a hidden `id` and a submit button. The script can still take over later.

**2. preventDefault after an await.**

```js
document.querySelector("form.add").addEventListener("submit", async (event) => {
  await Promise.resolve();
  event.preventDefault();
});
```

Symptom: the page reloads anyway, as if the script weren't there. By the time `preventDefault` runs, the browser has already started the submit. Fix: call `event.preventDefault()` as the first line of the listener.

**3. Failing silently.**

```js
async function addQuietly(form) {
  const response = await fetch(form.action, { method: "POST", body: new FormData(form) });
  if (response.ok) {
    document.querySelector("#cart-count").textContent = "1";
  }
}
```

Symptom: when the server is busy on a sale day, the button does nothing, and the customer thinks Golu is in their cart. Fix: catch every failure, including `!response.ok`, and show a message in a live region.

**4. Guessing features from the browser's name.**

```js
const canFetch = !navigator.userAgent.includes("Android");
```

Symptom: modern Android phones get the slow version, and some old browsers get code they can't run. Fix: test the feature itself: `typeof window.fetch === "function"`.

**5. Content that only the script can reveal.**

```css
.reviews {
  display: none;
}
```

Symptom: the script was meant to show the reviews. When it doesn't run, the reviews are invisible to customers and to search engines. Fix: show content by default, and let the script hide or fold it once it's running.
