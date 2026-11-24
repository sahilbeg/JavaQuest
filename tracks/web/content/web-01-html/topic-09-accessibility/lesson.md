## Hook

Close your eyes and ask a friend to read a web page to you. Now ask them to read only what's in the HTML: "image, yarn border dot png", "link, click here". You'd be lost in a minute.

That's how many of Dhaaga's customers meet the page. Some can't see the screen. Some can't use a mouse. Some zoom the page to twice its size. A page that works for all of them is **accessible**, and HTML gives you most of it for free, if you use the right elements.

## Concept

**Accessibility** means building pages that everyone can use, whatever their body or device. People often shorten it to **a11y**: an "a", then 11 letters, then a "y".

```callout jargon
**Assistive technology** is any tool that helps someone use a computer: a screen reader, a keyboard instead of a mouse, a switch, a screen magnifier, or voice control.
```

### Who this is for

Accessibility helps far more people than you'd think:

- **Blind** people and people with low vision use screen readers or zoom.
- People with a **shaky hand** or an injury use only the keyboard.
- **Older** customers, like many of Meera Aunty's friends, need bigger text and clear links.
- **Everyone** on a cracked phone, in bright sun, or on slow 3G that drops the photos.

Many countries have laws that ask websites to be accessible. Mostly, though, it's good business: every customer who can't order is a sale lost.

### What a screen reader hears

From your HTML, the browser builds a second, simpler tree for assistive technology: the **accessibility tree**. For each thing on the page, it notes a **role** (what it is: link, button, heading, image) and a **name** (what it's called: the link text, the alt text, the label).

```callout jargon
The **accessible name** is the words a screen reader says for an element. For a link, it's the link text. For an image, it's the alt text. For a field, it's the label.
```

The screen reader reads that tree aloud. If a name is missing, it guesses, usually badly:

```animate
title: From HTML to spoken words
kind: scene
nodes:
  - { id: html, label: "Your HTML", emoji: "📄", row: 0, col: 0 }
  - { id: browser, label: Browser, emoji: "🌐", row: 0, col: 2 }
  - { id: tree, label: "Accessibility tree", emoji: "🌳", row: 1, col: 2 }
  - { id: reader, label: Screen reader, emoji: "🔊", row: 1, col: 4 }
steps:
  - say: The browser reads your HTML and builds the page you see.
    send: { from: html, to: browser, label: "HTML" }
  - say: It also builds the accessibility tree. Each item gets a role and a name.
    show: [tree]
    send: { from: browser, to: tree, label: "role + name" }
  - say: For <img src="yarn-border.png"> with no alt, there's no name. The screen reader falls back to the file name.
    send: { from: tree, to: reader, label: "image, yarn border dot png" }
  - say: With alt="", the image is marked as decoration. The screen reader skips it completely.
    set: { tree: "border: hidden" }
  - say: For a link with real words, the role and name together make sense on their own.
    send: { from: tree, to: reader, label: "link, Read how Meera Aunty started" }
```

### Syntax: an informative image

```html
<img src="images/golu.jpg" alt="Golu, a small crocheted elephant in Rani pink yarn">
```

An **informative** image tells the visitor something: a product photo, a chart, a map. Its alt text says what someone would miss without it.

### Syntax: a decorative image

```html
<img src="images/yarn-border.png" alt="">
```

A **decorative** image is only there to look nice: a border, a swirl, a background pattern. Give it an **empty** alt, `alt=""`, with nothing between the quotes. Screen readers then skip it.

Don't leave `alt` out. With no `alt` at all, many screen readers read the file name instead. `alt=""` and no alt are very different things.

```callout tip
Ask: "If this picture vanished, would anyone miss information?" Yes means describe it. No means `alt=""`.
```

### Syntax: an image inside a link

```html
<a href="https://wa.me/919876543210"><img src="images/whatsapp.png" alt="Chat with us on WhatsApp"></a>
```

When a link holds only a picture, the picture's alt becomes the link's name. So the alt must say **where the link goes**, not what the icon looks like. "Green phone icon" doesn't help. "Chat with us on WhatsApp" does.

### Syntax: link text that stands alone

```html
<p><a href="story.html">Read how Meera Aunty started</a></p>
```

Screen reader users often pull up a list of every link on the page, and choose from it. Out of context, "click here", "read more" and "here" all sound the same. Put the meaning inside the link. As a bonus, a longer link is easier to tap.

### Use real buttons and links

```html
<button type="button">Show more colours</button>
```

You can make a `div` or a `span` look exactly like a button with CSS. Don't. A real `button` comes with three things a `div` doesn't have:

- The **Tab** key can reach it. A keyboard user moves through the page with Tab, and a `div` is skipped.
- **Enter** and **Space** press it.
- Screen readers say "button", so the listener knows they can press it.

The same goes for links: use `a` with an `href` for anything that goes somewhere. Use a `button` for anything that does something on the page. The rule in one line: **if HTML has an element for it, use that element.**

```callout jargon
**Keyboard focus** is the element that will react when you press a key. Tab moves focus to the next link, button or field. Browsers draw a ring around the focused element.
```

### Labels, headings and landmarks

You've already done three big accessibility jobs in this project:

- **Labels** joined to every field, so a screen reader says "Phone number, edit text" instead of just "edit text".
- **Headings** in order, so listeners can skim the outline.
- **Landmarks**, `header`, `nav`, `main` and `footer`, so they can jump straight to a part.

Keep checking those three on every page you build.

### Syntax: language of parts

```html
<p>Dhaaga means thread in Hindi: <span lang="hi">धागा</span>.</p>
```

The page's `lang="en"` tells screen readers to use an English voice. A word in another language read with English sounds can be meaningless. Wrap it in an element with its own `lang`. `hi` is Hindi, and `mr` is Marathi.

### A word about ARIA

You'll see attributes starting with `aria-`, like `aria-label="Open cart"`. **ARIA** adds names and roles that HTML can't express. It's powerful, but it's easy to get wrong. The first rule of ARIA is: don't use ARIA when a plain HTML element does the job. A later project covers it properly.

### Check it yourself

You don't need special tools to start:

1. **Unplug the mouse.** Press Tab through the page. Can you reach and use every link, button and field? Can you always see where you are?
2. **Turn on a screen reader.** VoiceOver on a Mac or iPhone, TalkBack on Android, NVDA on Windows (free). Listen to your page with your eyes closed.
3. **Zoom to 200%.** Does everything still fit and work?

## Worked example

Here's the footer of Golu's item page, before and after an accessibility check:

```html
<!-- Before -->
<footer>
  <img src="images/swirl.png">
  <p>Questions? <a href="https://wa.me/919876543210">Click here</a>.</p>
  <a href="https://www.instagram.com/"><img src="images/insta.png"></a>
</footer>

<!-- After -->
<footer>
  <img src="images/swirl.png" alt="">
  <p><a href="https://wa.me/919876543210">Ask us a question on WhatsApp</a></p>
  <a href="https://www.instagram.com/"><img src="images/insta.png" alt="Dhaaga on Instagram"></a>
</footer>
```

1. The yarn swirl is decoration, so it gets `alt=""` and the screen reader skips it.
2. "Click here" becomes "Ask us a question on WhatsApp", which makes sense alone.
3. The Instagram icon is the only thing in its link. Its alt says where the link goes.

## Try it

Paste this into the editor's body, then press Tab a few times in the preview:

```html
<p><a href="#top">A real link</a></p>
<button type="button">A real button</button>
<div class="button">A fake button</div>
<p><label>Try typing here <input type="text"></label></p>
```

- Tab stops on the link, the button and the field. It never stops on the fake button.
- Click into the preview first if Tab moves around the editor instead.
- Turn on your computer's screen reader and listen to each one.

## Recap

- Accessible pages work for everyone, including people using screen readers, keyboards and zoom.
- Informative images get alt text that describes them. Decorative images get `alt=""`.
- An image alone in a link gets alt text that says where the link goes.
- Link text must make sense on its own. Use real `button` and `a` elements, never clickable `div`s.
- Mark words in another language with their own `lang`, and test your page with Tab and a screen reader.

## Common mistakes

**1. Leaving alt out on a decoration.**

```html
<img src="images/yarn-border.png">
```

Many screen readers then read the file name: "yarn border dot png". Write `alt=""` so it's skipped.

**2. Describing the icon instead of the link.**

```html
<a href="https://wa.me/919876543210"><img src="images/whatsapp.png" alt="green phone icon"></a>
```

The listener hears "link, green phone icon" and still doesn't know what it does. Describe the link's purpose: `alt="Chat with us on WhatsApp"`.

**3. Making a div clickable.**

```html
<div class="button" onclick="showColours()">Show more colours</div>
```

A keyboard user can't Tab to it, and a screen reader doesn't call it a button. Use `<button type="button">Show more colours</button>`.

**4. "Click here" links.**

```html
<p>To read our story, <a href="story.html">click here</a>.</p>
```

In a list of links, this is just "click here". Move the meaning into the link: `<a href="story.html">Read our story</a>`.
