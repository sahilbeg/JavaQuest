## What happened

It is the night before the Diwali sale. The deploy of dhaaga.in is red in three places, and every one of them works on Kavya's Mac.

That is the clue. Each bug depends on something that is true on her laptop and false on the server:

1. `src/main.js` imports `./Cart.js`, but the file is `cart.js`. Her Mac does not care about capital letters. The Linux server does.
2. The UPI ID lives in Kavya's `.env.local`, which Git ignores, as it should. The server never got it, so the QR code says `pa=undefined`.
3. Golu's photo is listed in the catalogue as `images\golu.png`, typed on Sana's Windows laptop. On a web server, `\` is not a folder separator.

```callout analogy
A recipe that says "use the flour on the left shelf" works in your own kitchen. In a friend's kitchen, it fails. Good code says exactly what it needs, in a way every kitchen understands.
```

Kavya wrote a small pre-deploy check, so problems show up before deploy day. But the check is as forgiving as her Mac. Press Run to see it:

```text
imports: GREEN (src/cart.js)
environment: GREEN (VITE_API_URL, VITE_UPI_ID)
images: GREEN (/shop/images\golu.png)
```

All green, and all wrong. Your job is to make the check as strict as the server, so all three problems show up on every laptop.

## What you need

This builds on ticket 9 (configuration) and ticket 10 (builds).

```animate
title: One import, two disks
kind: scene
nodes:
  - { id: code, label: "import \"./Cart.js\"", emoji: "📄" }
  - { id: mac, label: Kavya's Mac, emoji: "💻", value: has cart.js }
  - { id: linux, label: Deploy server (Linux), emoji: "🖥️", value: has cart.js }
steps:
  - say: main.js asks for ./Cart.js, with a capital C. The real file is cart.js.
    focus: [code]
  - say: On the Mac, the disk ignores case when it looks up a name. Cart.js and cart.js are the same file to it.
    send: { from: code, to: mac, label: "Cart.js?" }
    set: { mac: "found cart.js ✓" }
  - say: On the Linux server, the disk compares names exactly. Cart.js is a different name, and no such file exists.
    send: { from: code, to: linux, label: "Cart.js?" }
    set: { linux: "not found ✗" }
  - say: Same code, two answers. A check that ignores case agrees with the Mac. It has to agree with the server.
    focus: [linux]
```

### Syntax: comparing names exactly

```js
const found = files.find((file) => file === wanted);
```

`===` on text compares every character, capitals included. `"src/Cart.js" === "src/cart.js"` is `false`, just like on the server.

Comparing with `toLowerCase()` on both sides ignores case, like the Mac. That is still useful, but only for a friendly hint after the exact search fails: "did you mean src/cart.js?"

```callout jargon
**Case-sensitive** means capital and small letters count as different. Linux file names are case-sensitive. Mac and Windows file names are usually **case-insensitive**: they keep your capitals but ignore them when looking a name up.
```

### Syntax: collecting before you throw

```js
const missing = names.filter((name) => read(env, name) === undefined);
if (missing.length > 0) {
  throw new Error(`Missing environment values: ${missing.join(", ")}`);
}
```

When two settings are missing, report both at once. Otherwise the deploy fails, someone adds one, deploys again, and only then hears about the second. `read(env, name)` is the finished helper from ticket 9: it gives `undefined` for a missing or blank value.

### Syntax: turning slashes around

```js
const clean = path.replaceAll("\\", "/");
```

Windows writes folders with `\`. Web addresses always use `/`. `replaceAll` swaps every one. In JavaScript text, `"\\"` is a single backslash, because `\` starts an escape.

A path from someone's own disk, like `C:\Users\sana\dhaaga\public\golu.png` or `/Users/kavya/dhaaga/public/golu.png`, can never work on the server. Refuse it with an error, so it is fixed in the catalogue.

```callout jargon
**BASE_URL** is where the site lives on the server. For dhaaga.in it is usually `/`. During the sale, the store runs under `dhaaga.in/shop/`, so it is `/shop/`. Every asset address must start with it.
```

## The tiers

1. **Bronze** resolves the incident. In `src/imports.js`, `resolveImport(fromFile, specifier, files)` finds the file only when the name matches exactly, capitals included. When nothing matches, it throws an `Error` starting `Cannot find`. If a file matches when case is ignored, the message adds `did you mean` and that file's path, like `did you mean src/cart.js?`.
2. **Silver**: in `src/env.js`, `requireEnv(env, names)` returns an object with the trimmed value of each name asked for, and nothing else. If any are missing or blank, it throws one `Error` that names every missing one, in the order asked, joined with `, `.
3. **Gold**: in `src/assets.js`, `assetUrl(base, path)` builds a site address:
   - backslashes become `/`;
   - a leading `./` or `/` on the path is dropped, and the base ends in exactly one `/`, so `assetUrl("/shop", "/images/golu.png")` is `/shop/images/golu.png`;
   - a path from one computer throws an `Error` whose message contains `one computer`: a drive letter like `C:\` or `D:/`, a path starting `/Users/` or `/home/`, or anything starting `file:`.

Change only the code under each `TODO`. The checks are the deploy server: make them all green. Then Run the pre-deploy check again: imports and environment should now be RED, with clear messages, on your laptop too.

## Recap

- "Works on my machine" means the code relies on something only that machine has. Find it, and make every machine behave like the server.
- Linux file names are case-sensitive. Compare names exactly, and use a case-insensitive match only to suggest a fix.
- Settings on your laptop, like `.env.local`, never reach the server. Check required settings and report every missing one at once.
- Web addresses use `/`. Turn `\` around, join the base and the path with exactly one `/`, and refuse paths from someone's own disk.

## Common mistakes

**1. Lower-casing everything to make it match.** `resolveImport` then agrees with the Mac again, and the deploy stays red. Compare exactly; use lower case only for the "did you mean" hint.

**2. Renaming the file on a Mac.** Renaming `cart.js` to `Cart.js` in Finder can leave Git's record unchanged, because the disk sees the same name. Fix the import instead. If you must rename, use `git mv cart.js Cart.js`.

**3. Throwing at the first missing setting.** The deploy fails, someone adds `VITE_UPI_ID`, deploys again, and only then hears about `VITE_API_URL`. Collect every missing name, then throw once.

**4. Joining with a plain +.** `"/" + "/images/golu.png"` gives `//images/golu.png`. A browser reads `//images` as a different website called `images`. Drop the leading `/` from the path before joining.

**5. Replacing only the first backslash.** `path.replace("\\", "/")` changes only the first one, so `images\diwali\banner.png` still breaks. Use `replaceAll`.
