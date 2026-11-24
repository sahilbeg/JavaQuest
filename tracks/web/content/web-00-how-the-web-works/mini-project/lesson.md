## The design

The design arrived with the ticket. On a phone, the card reads from top to bottom, like this:

```text
+----------------------------------------+
|  Dhaaga & Co.                          |
|  Handmade crochet coasters, keychains  |
|  and toys, made in Pune.               |
|                                        |
|  FC Road stall: Saturday and Sunday,   |
|  10:00 to 20:00                        |
|                                        |
|            [ UPI QR code ]             |
|                                        |
|      [ Message us on WhatsApp ]        |
+----------------------------------------+
```

The colours are already done. The design's `style.css` sits next to `index.html` and is linked from its head. Your job is the content in `<body>`.

```callout analogy
The page is the paper card, and the HTML is what's printed on it. A card nobody can call or pay from is just decoration, so the QR code and the WhatsApp link matter most.
```

## What you build

Five pieces, in this order, inside `<body>`:

1. One `<h1>` that says `Dhaaga & Co.`
2. A `<p>` that says what Dhaaga makes, using the word **crochet**.
3. A `<p>` with where and when: the **FC Road** stall, **Saturday and Sunday**, **10:00** to **20:00**.
4. The UPI QR code as an `<img>`, from the file `upi-qr.svg`, with alt text that mentions **UPI**.
5. A link to `https://wa.me/919876543210` that says `Message us on WhatsApp`.

You know the heading and paragraphs from WEB-0005. The picture and the link are new.

### Syntax: an image

```html
<img src="upi-qr.svg" alt="UPI QR code to pay Dhaaga & Co.">
```

- `<img>` puts a picture on the page. It has no closing tag, because it has no text inside.
- `src` is the path to the picture file. `upi-qr.svg` sits right next to `index.html`, so the path is just its name.
- `alt` is the **alt text**: words that describe the picture.

```callout jargon
**Alt text** describes a picture in words. A **screen reader**, an app that reads the page aloud for people who can't see it, reads the alt text instead of the picture. Browsers also show it when the picture fails to load.
```

Good alt text says what the picture is for, not just what it looks like. "UPI QR code to pay Dhaaga & Co." helps a customer more than "black and white squares".

### Syntax: a link

```html
<a href="https://wa.me/919876543210">Message us on WhatsApp</a>
```

- `<a>` makes a link. People tap the text between the tags.
- `href` is where the link goes.
- The link text should say what happens when you tap it. "Message us on WhatsApp" is clear. "Click here" isn't.

### WhatsApp links

WhatsApp opens a chat from any link of this shape:

```text
https://wa.me/<country code><phone number>
```

India's country code is `91`. Dhaaga's number is `98765 43210`, so the link is `https://wa.me/919876543210`. Leave out the spaces, the `+` and any dashes. Only digits go after `wa.me/`.

### How a customer uses the card

```animate
title: From the stall table to a WhatsApp chat
kind: scene
nodes:
  - { id: stall, label: Stall QR sign, emoji: "🧶", col: 0 }
  - { id: phone, label: "Customer's phone", emoji: "📱", value: "camera", col: 1 }
  - { id: page, label: Visiting card, emoji: "📄", row: 1, col: 1 }
  - { id: whatsapp, label: WhatsApp, emoji: "💬", col: 2 }
steps:
  - say: A customer at the stall scans the sign's code with their phone's camera. It holds the card's URL.
    send: { from: stall, to: phone, label: "card URL" }
    set: { phone: "opening page" }
  - say: The browser fetches the page. The customer sees the name, what Dhaaga makes and the opening hours.
    show: [page]
    send: { from: phone, to: page, label: "GET /" }
    set: { page: "name, hours, QR" }
  - say: They want to pay. The UPI QR code is right there on the card, ready for any UPI app.
    focus: [page]
    set: { page: "paying by UPI" }
  - say: Then they want a custom tote bag. They tap Message us on WhatsApp.
    send: { from: page, to: whatsapp, label: "wa.me/919876543210" }
  - say: WhatsApp opens a chat with Dhaaga's number already filled in. No typing numbers, no typos.
    set: { whatsapp: "chat with Dhaaga" }
    focus: [whatsapp]
```

```callout tip
The QR picture in this ticket, `upi-qr.svg`, is a stand-in that can't be scanned. The real one, from Dhaaga's UPI app, will replace it later. Because the file name stays the same, your HTML won't need to change.
```

## Recap

- A visiting card page is a heading, a few short paragraphs, a picture and a link.
- `<img src="..." alt="...">` shows a picture. It has no closing tag, and its alt text describes what the picture is for.
- `<a href="...">text</a>` makes a link. The text says what happens when you tap it.
- A WhatsApp link is `https://wa.me/` followed by the country code and number, digits only.

## Common mistakes

**1. Leaving out the alt text.**

```html
<img src="upi-qr.svg">
```

A screen reader may read out the file name, "upi-qr dot svg", or just "image". A customer who can't see the picture won't know it's for paying. Add `alt="UPI QR code to pay Dhaaga & Co."`.

**2. Spaces or a plus in the WhatsApp link.**

```html
<a href="https://wa.me/+91 98765 43210">Message us on WhatsApp</a>
```

A URL can't hold spaces, and WhatsApp wants digits only, so this link may show an error instead of opening a chat. Use digits only: `https://wa.me/919876543210`.

**3. Closing an image tag.**

```html
<img src="upi-qr.svg" alt="UPI QR code to pay Dhaaga & Co."></img>
```

`<img>` never has a closing tag. The browser quietly ignores `</img>`, but it shows you misunderstood the element. Remove it.

**4. A link with no text.**

```html
<a href="https://wa.me/919876543210"></a>
```

The link is on the page, but it's invisible, so nobody can tap it. Put the words people should see between the tags.
