## Hook

When a scooter won't start, a good mechanic doesn't guess. They plug in a tester and read what the engine is actually doing.

Every browser has a tester like that built in. It's called **DevTools**, short for developer tools. It shows you the page's real HTML, every message the page's JavaScript prints, and every file the browser asked for.

Most people never open it. Web developers have it open all day. Once you know it, a "broken" page stops being a mystery. You just look.

## Concept

DevTools has many panels, but three do most of the work: **Elements**, **Console** and **Network**. This lesson uses Chrome. Edge looks almost the same, and Firefox and Safari have the same panels with slightly different names.

### Opening DevTools

Open any page, then use one of these:

| Computer | Shortcut |
|---|---|
| Mac | Cmd+Option+I |
| Windows or Linux | F12, or Ctrl+Shift+I |
| Any | Right-click part of the page and choose **Inspect** |

A panel opens beside or below the page. Tabs along its top say **Elements**, **Console**, **Network** and more. Press the same shortcut again to close it.

```callout tip
In Safari, DevTools is hidden at first. Turn on the developer features in Safari's Settings, under Advanced, then use Cmd+Option+I.
```

### The Elements panel: the page's HTML, live

**Elements** shows the page's HTML as a tree. Each element is a line, and the elements nested inside it sit indented below it. Click the small triangles to open and close them.

Hover over a line and the browser highlights that part of the page. Click the arrow icon in the top-left corner of DevTools, then click anything on the page. The Elements panel jumps straight to its HTML.

```callout jargon
To **inspect** an element means to select it in DevTools and look at its HTML and CSS.
```

### Change the page, just for you

Double-click any text in the Elements panel and type something new. The page changes at once.

This is great for trying ideas. But the change lives only in your browser, only until you reload. The real file on the server never changes. So it's safe to play.

### Styles: the CSS for what you picked

Next to the HTML, the **Styles** pane lists every CSS rule that applies to the selected element. You can click a colour and pick a new one, or untick a rule to switch it off. Again, nothing is saved. It's a sandbox for trying looks before you write them into your files.

### The Console: messages from JavaScript

The **Console** shows what the page's JavaScript prints with `console.log`. It also shows errors, in red, with the file and line where they happened:

```text
Uncaught ReferenceError: totl is not defined
    at main.js:4:13
```

That says line 4 of `main.js` uses a name, `totl`, that doesn't exist. It's probably a typo for `total`. The `13` after the line number is the column: how many characters along the line the problem starts.

You can also type JavaScript into the Console and press Enter. Try `150 * 4` and it answers `600`. It's a calculator that speaks JavaScript.

### The Network panel: every request

The **Network** panel lists every request the page makes: the HTML, the CSS, the JavaScript and every photo. Each row shows the file's name, its status code, its size and how long it took.

It only records while it's open. So open the Network panel first, then reload the page.

Rows with a `404` or `500` status turn red. That's usually the first place to look when part of a page is missing.

```callout tip
The menu that says **No throttling** can slow the page down on purpose. Use it to see how Dhaaga's page feels on a weak phone signal at the stall.
```

### Device mode: see it like a phone

Most of Dhaaga's customers will open the page on a phone. Click the phone-and-tablet icon at the top of DevTools, or press Cmd+Shift+M (Ctrl+Shift+M on Windows). The page shrinks to a phone's size.

Pick a phone model from the menu at the top. It's not a real phone, but it's close enough to spot most layout problems.

### Tracking down the broken photo

Here's how the panels solve the mystery of the missing coaster photo:

```animate
title: Finding the missing coaster photo
kind: scene
nodes:
  - { id: page, label: Test page, emoji: "📱", value: "broken image", row: 0, col: 0 }
  - { id: elements, label: Elements, emoji: "🌳", row: 0, col: 1 }
  - { id: network, label: Network, emoji: "📡", row: 0, col: 2 }
  - { id: folder, label: Site folder, emoji: "📁", value: "Coaster.JPG", row: 1, col: 2 }
steps:
  - say: The page loads, but where the coaster photo should be, there's only a small broken-image icon.
    focus: [page]
  - say: Right-click the broken image and choose Inspect. Elements shows its HTML, which asks for images/coaster.jpg.
    send: { from: page, to: elements, label: "Inspect" }
    set: { elements: "src=images/coaster.jpg" }
  - say: Open Network and reload. Every file is 200, except one red row. coaster.jpg came back 404 Not Found.
    focus: [network]
    set: { network: "coaster.jpg 404" }
  - say: Now look in the site's folder. The photo is there, but it's named Coaster.JPG, with capital letters.
    send: { from: network, to: folder, label: "compare names" }
    focus: [folder]
  - say: Servers treat capitals as different letters. The file is renamed to coaster.jpg, matching the HTML.
    set: { folder: "coaster.jpg" }
  - say: Reload again. The Network row turns 200, and the photo shows. Mystery solved by looking, not guessing.
    set: { network: "coaster.jpg 200", page: "photo shows" }
    focus: [page, network]
```

## Worked example

Here's the same hunt, written out as you would do it.

1. Open the test page in Chrome and press Cmd+Option+I.
2. Right-click the broken image and choose **Inspect**. The Elements panel highlights this line:

```html
<img src="images/coaster.jpg" alt="A set of four crochet coasters">
```

3. Click the **Network** tab, then reload with Cmd+R. The list fills up:

```text
Name           Status
index.html     200
style.css      200
coaster.jpg    404
```

4. The red row says the server has no `images/coaster.jpg`.
5. Open the site's folder. The photo is called `Coaster.JPG`.
6. Rename it to `coaster.jpg`, reload, and check the Network row again. It now says `200`, and the photo appears.

The Console would also show a red line for the failed request. Two panels, one story.

## Try it

Open any website you like in Chrome, then open DevTools.

1. **Elements:** right-click the biggest heading and choose **Inspect**. Double-click its text in the Elements panel and type "Dhaaga & Co.". Then reload, and watch your change disappear.
2. **Console:** click the Console tab. Type `150 * 4` and press Enter. Then type `console.log("Namaste!")` and press Enter.
3. **Network:** click the Network tab and reload the page. Count how many requests it made. Click one row and look at its status code.
4. **Device mode:** press Cmd+Shift+M and pick a phone. Scroll the page the way a customer would.

```callout tip
If the page and DevTools feel cramped, the menu with three dots in DevTools lets you move the panel to the bottom, the side, or its own window.
```

## Recap

- **DevTools** is the browser's built-in toolbox. Open it with Cmd+Option+I, F12, or right-click and **Inspect**.
- **Elements** shows the page's HTML and CSS. Edits there are only in your browser, until you reload.
- **Console** shows what JavaScript prints, plus errors in red with a file and line number.
- **Network** lists every request with its status code. Open it before you reload.
- **Device mode** shows the page at a phone's size.

## Common mistakes

**1. Thinking an edit in Elements was saved.**

You fix a typo in the Elements panel, and the page looks right. Then you reload, and the typo is back. Elements only changes your browser's copy. Make the same change in the real file in your editor.

**2. Opening Network after the page has loaded.**

```text
Recording network activity…
Perform a request or hit ⌘ R to record the reload.
```

The panel only records while it is open, so it starts empty. Leave it open and reload the page.

**3. Not reading the red error.**

```text
Uncaught ReferenceError: totl is not defined
    at main.js:4:13
```

It's tempting to close the Console and guess. But the error names the problem and the exact line. Open `main.js`, go to line 4, and fix the name.

**4. Seeing an old version after you changed a file.**

Browsers keep copies of files to load pages faster. Sometimes they show the old copy. Press Cmd+Shift+R (Ctrl+Shift+R on Windows) for a hard reload, which fetches every file fresh.
