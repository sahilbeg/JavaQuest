## What happened

Meera Aunty's nephew made a price board page for the stall, so customers can check prices on their phones. On his laptop it looks perfect:

```text
Coaster set (4): ₹150
Keychain: ₹120
```

On the phone at the stall, it looks like this:

```text
Coaster set (4): â‚¹150
Keychain: â‚¹120
```

Every ₹ sign has turned into three strange characters. The numbers are fine. Only the rupee sign is broken. Customers are confused, and the lunchtime crowd is coming.

```callout analogy
An incident is a problem hurting customers right now. Stop the hurt first, then make the fix solid.
```

## What you need

### Letters are stored as numbers

A file on disk holds only numbers, called **bytes**. Each byte is a number from 0 to 255. To show text, the browser needs a table that says which numbers mean which letters.

That table is called a **character encoding**. If the page is saved with one table and read with another, the numbers turn into the wrong letters.

```callout jargon
A **byte** is one small number, from 0 to 255. A **character encoding** is the table that turns bytes into letters and back. **UTF-8** is the encoding that covers every language: English, Hindi, Marathi, ₹ and even emoji.
```

### One sign, three bytes

Plain English letters each take one byte in UTF-8. Other signs take more. The ₹ sign takes three bytes:

```animate
title: The ₹ sign is three bytes
kind: code
language: js
code: |
  const bytes = new TextEncoder().encode("₹");
  console.log(bytes.length);
  console.log(bytes[0], bytes[1], bytes[2]);
steps:
  - line: 1
    vars: { bytes: "[226, 130, 185]" }
    say: TextEncoder turns text into UTF-8 bytes. The single ₹ sign becomes three numbers.
  - line: 2
    out: "3"
    say: One sign on screen, but three bytes in the file.
  - line: 3
    out: "226 130 185"
    say: These are the three numbers that mean ₹ in UTF-8. A browser reading with UTF-8 turns them back into one ₹.
```

You can check the whole price too. `"₹150"` is 4 characters but 6 bytes:

```js run
const price = "₹150";
console.log(price.length);
console.log(new TextEncoder().encode(price).length);
```

### When the browser guesses wrong

The nephew's page never says which table it uses. So each browser has to guess. Some guess UTF-8. The phone at the stall guessed an old Western table, where each byte is one letter:

```animate
title: Three bytes, read with the wrong table
kind: scene
nodes:
  - { id: file, label: index.html, emoji: "📄", value: "226 130 185", row: 0, col: 0 }
  - { id: guess, label: "Browser, guessing", emoji: "🤔", row: 0, col: 1 }
  - { id: screen, label: "Stall phone", emoji: "📱", value: "blank", row: 0, col: 2 }
  - { id: told, label: "Browser, told UTF-8", emoji: "✅", row: 1, col: 1 }
steps:
  - say: The file holds the three bytes for ₹. Nothing in the page says which table to read them with.
    focus: [file]
  - say: The phone's browser guesses an old table where every byte is one letter. 226 is â, 130 is ‚, 185 is ¹.
    send: { from: file, to: guess, label: "226 130 185" }
    set: { guess: "3 letters" }
  - say: So the phone shows â‚¹150. The bytes were right all along. They were just read with the wrong table.
    send: { from: guess, to: screen, label: "â‚¹" }
    set: { screen: "â‚¹150" }
  - say: Now the page says charset utf-8. The browser stops guessing and reads the same three bytes as UTF-8.
    show: [told]
    send: { from: file, to: told, label: "226 130 185" }
    set: { told: "1 sign" }
  - say: Three bytes become one ₹. Same file, same bytes, right answer.
    send: { from: told, to: screen, label: "₹" }
    set: { screen: "₹150" }
```

```callout warning
Your own browser may guess right, so the page can look fine in the preview. That's why this bug hides so well. The checks read the file itself.
```

### Syntax: the charset line

```html
<meta charset="utf-8">
```

This line tells the browser: "this file is UTF-8, don't guess". It goes inside `<head>`, and has no closing tag.

### Why it must come first

Browsers look for the charset line only near the very top of the file: within the first **1024 bytes**. If it comes later, the browser may have already started reading with a guess.

So make it the very first thing inside `<head>`, before the `<title>` and before any `<style>`. Then it's always within the first 1024 bytes, however long the page grows.

```html
<head>
  <meta charset="utf-8">
  <title>Dhaaga & Co.</title>
  ...
</head>
```

### Syntax: the lang attribute

```html
<html lang="en">
```

The `lang` attribute says which language the page is written in. `en` means English. Screen readers use it to pick the right voice, and browsers use it to offer translation. A Marathi page would say `lang="mr"`, and a Hindi page `lang="hi"`.

### Syntax: the viewport line

```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```

Without this line, a phone pretends to be a wide laptop screen, then shrinks the whole page to fit. The prices come out tiny. With it, the phone uses its real width, so the text stays readable.

```callout tip
Every page you start from now on begins with the same few lines: the doctype, `<html lang="en">`, then the charset line first in `<head>`, then the viewport line and the title.
```

## The tiers

The page is `src/index.html`. Each tier is a group of checks. Fix them in order.

1. **Bronze** resolves the incident: the page has `<meta charset="utf-8">`, and the prices still use the ₹ sign.
2. **Silver**: the charset line is the very first element inside `<head>`, within the first 1024 bytes of the file.
3. **Gold**: `<html>` has `lang="en"`, and the head has the viewport line, with `width=device-width` and `initial-scale=1`.

Bronze earns the most XP. Silver and gold each add a bonus.

## Recap

- Files store letters as numbers. A **character encoding** is the table between them, and **UTF-8** covers every language, ₹ included.
- Without `<meta charset="utf-8">`, browsers guess the table, and some guess wrong.
- The charset line goes first in `<head>`, within the first 1024 bytes of the file.
- `lang="en"` on `<html>` names the page's language, and the viewport line makes phones use their real width.

## Common mistakes

**1. Hiding the ₹ instead of fixing it.**

```html
<p>Coaster set (4): Rs. 150</p>
```

The garbage goes away, but so does the rupee sign, and the real bug is still there. The next special letter will break too. Keep the ₹ and add the charset line. The bronze checks look for the ₹ signs.

**2. Putting the charset line at the end of the head.**

```html
<head>
  <title>Dhaaga & Co. price board</title>
  <style>
    /* ... a long block of CSS ... */
  </style>
  <meta charset="utf-8">
</head>
```

The page has a long `<style>` block, so this line lands after the first 1024 bytes. Many browsers never see it. Move it to be the first line inside `<head>`.

**3. Spelling the charset differently.**

```html
<meta charset="utf8">
```

Many browsers forgive `utf8`, without the dash, but the HTML standard asks for `utf-8`. The checks want the standard spelling. Capital letters are fine: `UTF-8` works too.
