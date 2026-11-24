## Hook

Open any newspaper. The paper's name sits at the top in huge letters. Each story has a bold title, and some stories have smaller titles inside them. You can skim the whole front page in five seconds, just by reading the titles.

Dhaaga's catalogue page has no titles at all. It reads like one long letter. People who use screen readers skim by titles too, by jumping from heading to heading. On this page, there's nothing to jump to.

## Concept

Most of a web page is text. HTML has elements that say what each piece of text is: a title, a paragraph, an important word. Choosing the right one helps everyone: people skimming, screen readers, and search engines.

### Syntax: headings

```html
<h1>Dhaaga &amp; Co.</h1>
<h2>Our story</h2>
<h3>New this week</h3>
```

HTML has six heading elements, from `h1` to `h6`. The number is the **level**. `h1` is the most important, and `h6` is the least.

- `h1` is the name of the whole page. A page has exactly one.
- `h2` names each big part of the page, like "Our story" or "Catalogue".
- `h3` names a smaller part inside an `h2` part, and so on down.

```callout analogy
Headings work like a book's contents page. The book's title is the h1. Chapters are h2s. Sections inside a chapter are h3s.
```

### Choose headings by meaning, not size

Browsers show `h1` biggest and `h6` smallest. It's tempting to pick a heading because of its size. Don't. Pick the level that matches the page's outline, and change the size later with CSS.

Likewise, don't make a paragraph bold and big to fake a heading. A screen reader can't tell it's a heading, so it can't jump to it.

### Syntax: heading order

```html
<h2>Catalogue</h2>
<h3>New this week</h3>
```

Headings go **down one level at a time**. Under an `h2`, the next level is `h3`, not `h4`. Going back up is fine: after an `h3`, you can start a new `h2` part.

Screen reader users often open a list of all the headings first. They use it like a contents page. A skipped level makes them think a part is missing. Watch a customer skim the catalogue page:

```animate
title: Skimming by headings
kind: scene
nodes:
  - { id: reader, label: Screen reader, emoji: "🔊", row: 0, col: 0 }
  - { id: h1, label: "h1 Dhaaga & Co.", emoji: "1️⃣", row: 0, col: 2 }
  - { id: story, label: "h2 Our story", emoji: "2️⃣", row: 1, col: 2 }
  - { id: cat, label: "h2 Catalogue", emoji: "2️⃣", row: 2, col: 2 }
  - { id: new, label: "h4 New this week", emoji: "4️⃣", row: 3, col: 3, value: "level 4?" }
steps:
  - say: Mrs. Desai is blind and uses a screen reader. She presses the key that jumps to the next heading.
    focus: [reader]
  - say: It lands on the h1 and reads "Dhaaga and Co., heading level 1". Now she knows whose page this is.
    send: { from: reader, to: h1, label: "next heading" }
  - say: The next jump reads "Our story, heading level 2". It's a big part of the page.
    send: { from: reader, to: story, label: "next heading" }
  - say: Then "Catalogue, heading level 2". Another big part.
    send: { from: reader, to: cat, label: "next heading" }
  - say: Then "New this week, heading level 4". She wonders where level 3 went, and whether she missed something.
    send: { from: reader, to: new, label: "next heading" }
  - say: Change it to an h3 and the outline makes sense again. Nothing looks different for sighted visitors, but the page now reads right.
    set: { new: "level 3" }
    focus: [new]
```

### Syntax: paragraphs and line breaks

```html
<p>Meera Kulkarni has crocheted all her life.</p>
<p>Shop: Aundh<br>Stall: FC Road</p>
```

`p` makes a paragraph. Browsers put a little space above and below each one.

Inside HTML, every run of spaces and new lines in your file counts as **one space**. Pressing Enter in your editor does not start a new line on the page. For a new line inside a paragraph, like the lines of an address, use `<br>`. It's a void element, like `<hr>`.

```callout warning
Don't use `<br><br>` to make gaps between paragraphs. Use separate `<p>` elements, and set the spacing with CSS later.
```

### Syntax: strong and em

```html
<p>Every piece is made <em>by hand</em>.</p>
<p><strong>Orders close on Friday.</strong></p>
```

- `strong` means "this is important". Browsers show it in **bold**.
- `em` means "say this with stress", like when you lean on a word. Browsers show it in *italics*.

Screen readers can change their voice for these, so pick them for meaning. You may also see `<b>` and `<i>`. They only change the look, with no extra meaning.

```callout jargon
**Emphasis** is the stress you put on a word when you say it out loud. "I said *Mint*, not Marigold" leans on Mint.
```

### Syntax: character references

```html
<p>Dhaaga &amp; Co.</p>
<p>Write &lt;your name&gt; in the notes.</p>
```

Some characters mean something special in HTML. `<` starts a tag, and `&` starts a **character reference**. To show these as plain text, you write a short code instead:

| You want | You write |
|---|---|
| `&` | `&amp;` |
| `<` | `&lt;` |
| `>` | `&gt;` |

```callout jargon
A **character reference** (often called an entity) is a code that starts with `&` and ends with `;`. The browser swaps it for one character. `&amp;` becomes `&`.
```

Other symbols, like `₹` and `धागा`, can be typed straight in. They work because the page has `<meta charset="utf-8">`, as you saw in the last project.

```callout tip
If words disappear from your page, look for a `<` in the text. The browser may have read the words after it as a tag it doesn't know.
```

## Worked example

Here's the start of an "About us" part for the Aundh shop:

```html
<h1>Dhaaga &amp; Co.</h1>
<h2>Visit the shop</h2>
<p>Our shop is in Aundh, Pune.<br>Open Tuesday to Sunday, 11:00 to 21:00.</p>
<h3>Parking</h3>
<p>Park on the lane <em>behind</em> the shop, <strong>not</strong> on the main road.</p>
<p>Prices are the same &amp; fixed at the stall and the shop.</p>
```

1. One `h1` names the page. `h2` opens the "Visit the shop" part.
2. The first paragraph uses `<br>` to put the hours on their own line.
3. `h3` is a smaller part inside "Visit the shop". It goes down one level, from 2 to 3.
4. `em` stresses "behind", and `strong` marks "not" as important.
5. `&amp;` shows an `&` sign as plain text.

## Try it

Paste this into the editor's body and watch the preview:

```html
<h1>Dhaaga &amp; Co.</h1>
<h2>New this week</h2>
<p>Golu is back
in Rani pink.</p>
<p>Use the code <GOLU> at the stall.</p>
```

- The new line inside the first paragraph turns into a space. Add `<br>` to break the line for real.
- The word GOLU vanishes from the second paragraph. Write it as `&lt;GOLU&gt;` and it comes back.
- Change `h2` to `h4`. The text gets smaller, but the outline is now wrong. Change it back.

## Recap

- Headings go from `h1` to `h6`. A page has one `h1`, and each level goes down one step at a time.
- Pick headings for the outline, not for their size. CSS changes the size later.
- `p` makes a paragraph, and `<br>` breaks a line inside one. Spaces and new lines in your file count as one space.
- `strong` marks important words, and `em` stresses a word.
- Write `&`, `<` and `>` as `&amp;`, `&lt;` and `&gt;` when you mean the plain characters.

## Common mistakes

**1. Skipping a heading level.**

```html
<h2>Catalogue</h2>
<h4>New this week</h4>
```

The page looks fine, but a screen reader announces level 4 straight after level 2, and the outline has a hole. Use `<h3>` and make it smaller with CSS later.

**2. Using two h1 elements.**

```html
<h1>Dhaaga &amp; Co.</h1>
<h1>Catalogue</h1>
```

Now the page has two names. Keep one `h1` for the page's name, and make the parts `h2`.

**3. Writing a < sign straight into the text.**

```html
<p>Tip: write <your name> in the notes.</p>
```

The browser reads `<your name>` as an element called `your`, and the words vanish. The page shows "Tip: write in the notes." Write `&lt;your name&gt;` instead.

**4. Pressing Enter to start a new line.**

```html
<p>Dhaaga &amp; Co.
Aundh, Pune</p>
```

The new line in the file shows as a space, so both parts appear on one line. Use `<br>` inside the paragraph, or two separate `p` elements.
