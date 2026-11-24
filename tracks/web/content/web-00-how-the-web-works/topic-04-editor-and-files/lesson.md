## Hook

Dhaaga keeps its yarn in a big wooden box with little sections. Marigold in one, Indigo in the next, hooks in a tin at the front. Each section has a paper label. When someone needs Rani pink, their hand goes straight to it.

Now imagine someone tips everything into one heap and peels off the labels. The yarn is still there, but finding anything takes forever.

A website is a box of files. If every file has a clear place and a clear name, the browser finds each one in a blink. If not, you get broken photos and missing colours. This ticket sets up your box.

## Concept

You need two things: a good editor to write your files, and a tidy folder to keep them in.

### A code editor

A **code editor** is an app for writing code. It colours the different parts of your code, so mistakes stand out. It also helps you type, and it saves **plain text**: exactly the letters you typed, nothing more.

Word processors like Microsoft Word are the wrong tool. They save hidden formatting, like fonts and bold, mixed in with your letters. A browser can't read that.

```callout jargon
**Plain text** is a file that holds only letters, numbers and symbols, with no hidden formatting. HTML, CSS and JavaScript files are all plain text.
```

### VS Code

**Visual Studio Code**, or **VS Code**, is a free code editor that most web developers use. It runs on Mac, Windows and Linux.

1. Download it from `code.visualstudio.com`.
2. On a Mac, drag it into your Applications folder, then open it.
3. Choose **File**, then **Open Folder…**, and pick your website's folder.

The left side shows your folder's files, called the **Explorer**. Click a file and it opens on the right, in a tab.

### A website is a folder

A website is a folder of files. Here is the Dhaaga site:

```text
dhaaga-site/
├── index.html
├── style.css
├── main.js
└── images/
    ├── coaster.jpg
    └── tote-bag.jpg
```

`dhaaga-site` is the **project folder**. It holds everything the site needs. `images` is a folder inside it, for photos.

```callout jargon
A **folder** (also called a **directory**) holds files and other folders. The **project folder** is the one folder that holds a whole project.
```

### File extensions say what is inside

The letters after the last dot in a file's name are its **extension**. They tell the editor and the browser what kind of file it is.

| Extension | What is inside |
|---|---|
| `.html` | A page's HTML |
| `.css` | CSS rules |
| `.js` | JavaScript |
| `.jpg`, `.png`, `.svg` | A picture |

Macs hide extensions by default. Turn them on in Finder: **Finder**, **Settings**, **Advanced**, then tick **Show all filename extensions**. Now you can see that a file is really `index.html` and not `index.html.txt`.

### index.html is the front door

When a URL ends at a folder, like `https://dhaaga.in/`, the server looks inside that folder for a file named `index.html`. That's the page it sends.

So the home page of every site you build will be called `index.html`. Spell it exactly like that, all lowercase.

### Paths: how one file finds another

When `index.html` wants a photo, it gives a **path**: directions from where it is to where the photo is.

```html
<img src="images/coaster.jpg" alt="Four crochet coasters">
```

`images/coaster.jpg` means: "from my own folder, go into `images`, then take `coaster.jpg`". The `/` separates each step.

Watch the browser follow those directions:

```animate
title: The browser follows a path
kind: scene
nodes:
  - { id: site, label: "dhaaga-site/", emoji: "📁", row: 0, col: 1 }
  - { id: index, label: index.html, emoji: "📄", row: 1, col: 0 }
  - { id: css, label: style.css, emoji: "🎨", row: 1, col: 1 }
  - { id: images, label: "images/", emoji: "📁", row: 1, col: 2 }
  - { id: photo, label: coaster.jpg, emoji: "🖼️", row: 2, col: 2 }
links:
  - { from: site, to: index }
  - { from: site, to: css }
  - { from: site, to: images }
  - { from: images, to: photo }
steps:
  - say: index.html says src="images/coaster.jpg". The browser starts where index.html lives, in dhaaga-site.
    focus: [index, site]
  - say: The first step is images. So the browser goes into the images folder.
    send: { from: site, to: images, label: "images/" }
    focus: [images]
  - say: The last step is coaster.jpg. It's right there, so the photo loads.
    send: { from: images, to: photo, label: "coaster.jpg" }
    set: { photo: "found" }
    focus: [photo]
  - say: For style.css, the path is just style.css, with no folder. It sits right next to index.html.
    send: { from: site, to: css, label: "style.css" }
    set: { css: "found" }
    focus: [css]
  - say: A path that names a folder that isn't there, like pics/coaster.jpg, finds nothing. The browser gets a 404.
    focus: [site]
```

This is called a **relative path**, because it is relative to the file that uses it. Two more pieces help:

- `../` means "go up one folder".
- A full URL, like `https://dhaaga.in/images/coaster.jpg`, is an **absolute** address. It works from anywhere.

### Names that never break

Your laptop may forgive sloppy names. A real server won't. Most servers treat `Coaster.jpg` and `coaster.jpg` as two different files. Spaces turn into `%20` in URLs and are easy to get wrong.

Follow three rules for every file and folder:

1. Use lowercase letters only.
2. Use dashes instead of spaces: `tote-bag.jpg`, not `Tote Bag.jpg`.
3. Use only letters, numbers, dashes and one dot before the extension.

```callout warning
A Mac doesn't care about capitals in file names, so a wrong capital works on your laptop. Then it breaks on the server. Lowercase everything and the problem never starts.
```

### Saving your work

The browser reads the file on your disk, not what's on your screen. Unsaved changes don't count.

In VS Code, an unsaved file shows a dot on its tab. Press Cmd+S (Ctrl+S on Windows) to save. You can also turn on **File**, **Auto Save**, and VS Code saves for you after every change.

### The terminal inside VS Code

The **terminal** is a window where you type commands instead of clicking. VS Code has one built in. Open it with **View**, **Terminal**, or press Ctrl and the backtick key together. The backtick key is just above Tab.

```callout jargon
The **terminal** runs commands you type. The **prompt**, often ending in `%` or `$`, means it's ready for your next command. A **command** is one instruction, like `ls`.
```

Four commands get you far:

| Command | What it does |
|---|---|
| `pwd` | Shows which folder you're in |
| `ls` | Lists what's in this folder |
| `mkdir images` | Makes a new folder called `images` |
| `cd images` | Moves into the `images` folder (`cd ..` moves back up) |

You'll use the terminal for Git in ticket WEB-0007.

### Seeing your page

The quickest way to see your page is to double-click `index.html` in Finder. It opens in your browser with an address like this:

```text
file:///Users/you/dhaaga-site/index.html
```

`file://` means the browser read the file straight from your disk, with no server involved. That's fine for simple pages. Inside Cadence, the preview beside the editor does this for you, every time you save.

## Worked example

Here is the Dhaaga site folder being made from VS Code's terminal. Lines starting with `%` are what you type. The rest is what the computer prints.

```text
% mkdir dhaaga-site
% cd dhaaga-site
% mkdir images
% touch index.html style.css main.js
% ls
images		index.html	main.js		style.css
% pwd
/Users/you/dhaaga-site
```

1. `mkdir dhaaga-site` makes the project folder.
2. `cd dhaaga-site` moves into it.
3. `mkdir images` makes a folder for photos.
4. `touch` makes empty files. Here it makes three at once.
5. `ls` lists what's there: one folder and three files.
6. `pwd` prints where you are. Each `/` is one step deeper.

Now choose **File**, **Open Folder…** and open `dhaaga-site`. All four items appear in the Explorer, ready to fill in.

## Try it

Set up your own box.

1. Install VS Code from `code.visualstudio.com` and open it.
2. Open the terminal with **View**, **Terminal**.
3. Type `cd ~` and press Enter. `~` is your home folder.
4. Make the folders and files from the worked example: `mkdir dhaaga-site`, `cd dhaaga-site`, `mkdir images`, then `touch index.html style.css main.js`.
5. Run `ls` and check that you see `images`, `index.html`, `main.js` and `style.css`.
6. Open the folder with **File**, **Open Folder…**. Click `index.html`, type `Hello, Dhaaga!`, and press Cmd+S.
7. Find `index.html` in Finder and double-click it. Your words appear in the browser.

```callout tip
If the dot on the tab is still there, the file isn't saved yet. Save, then reload the browser.
```

## Recap

- Write code in a **code editor** like VS Code, which saves **plain text**.
- A website is a **project folder** of files. Each file's **extension**, such as `.html`, `.css` or `.js`, says what's inside.
- `index.html` is the front door: the page a server sends when the URL ends at a folder.
- A **relative path** like `images/coaster.jpg` is read from the folder of the file that uses it.
- Name files in lowercase with dashes and no spaces. Save with Cmd+S. The **terminal** runs commands like `ls`, `cd` and `mkdir`.

## Common mistakes

**1. Capitals and spaces in a file name.**

```html
<img src="images/coaster photo.jpg" alt="Four crochet coasters">
```

The real file is `Coaster Photo.jpg`. It shows on your Mac, which ignores capitals. On the server it's a 404 and a broken image. Rename the file `coaster-photo.jpg` and use that exact name in the HTML.

**2. Writing the path from the wrong place.**

```html
<img src="dhaaga-site/images/coaster.jpg" alt="Four crochet coasters">
```

`index.html` already sits inside `dhaaga-site`. The browser looks for a `dhaaga-site` folder inside it, finds none, and gets a 404. Write the path from the HTML file's own folder: `images/coaster.jpg`.

**3. Forgetting to save.**

You change the hours, reload the browser, and nothing happens. The tab in VS Code still shows a dot. Press Cmd+S, then reload.

**4. A hidden .txt on the end.**

```text
index.html.txt
```

Some editors, like TextEdit, quietly add `.txt`. The browser then shows your HTML as plain text, tags and all. Turn on **Show all filename extensions** in Finder, rename the file to `index.html`, and write code in VS Code.
