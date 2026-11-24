## Hook

On your first day in a big restaurant kitchen, nobody expects you to know every recipe. You learn where the walk-in fridge is, who runs each station, and how an order travels from the waiter's pad to the plate. Then you can find anything.

Big codebases are the same. Professionals don't read 400 files from top to bottom. They build a rough map, follow one feature from end to end, and use search and tools to find the rest. This ticket teaches that map-making.

## Concept

### Don't read it all

```callout jargon
A **codebase** is all the code, configuration and tests for a project, usually in one Git repository. **Onboarding** is getting a new person productive in it.
```

You'll never hold a big codebase in your head, and nobody else does either. The goal is to answer three questions quickly:

1. **How does it start?** Which file runs first, and how do pages get chosen?
2. **Where do things live?** Which folder holds features, shared parts, data and tests?
3. **How does one thing work, end to end?** Pick a feature and trace it fully, once.

After that, you read code **when you need it**, for the ticket in front of you.

### Start with package.json

`package.json` is the front door. In [npm and packages](/t/web-07-tooling/topic-01-npm-and-packages) you met it as a list of packages. In a big codebase, it also tells you how the team works.

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "test": "vitest",
    "lint": "eslint src",
    "storybook": "storybook dev -p 6006"
  },
  "dependencies": { "react": "19.1.0", "react-router": "7.6.0", "@tanstack/react-query": "5.80.0" }
}
```

- **`scripts`** tell you how to run, test and build it. Run `npm run dev` and `npm test` on day one, and see them work.
- **`dependencies`** tell you the big choices: here, React Router for pages and TanStack Query for fetching data. Each one tells you where to look for a kind of code.
- A **`README.md`**, and a `docs/` folder if there is one, come next. Notice what's out of date, and fix it in your first PR.

### Find the entry and the routes

Every React app starts the same way. Follow the chain:

```text
index.html  ->  src/main.jsx  ->  <App />  ->  routes  ->  the page for each URL
```

`main.jsx` renders `<App />`, usually wrapped in providers for context, data and routing. The **routes** are the table of contents of the app: each URL and the page component it shows. You built a small router in [Pages and routing](/t/react-09-react-in-depth/topic-06-routing); real apps use React Router, and the idea is the same.

```jsx
<Route path="/" element={<HomePage />} />
<Route path="/products/:id" element={<ProductPage />} />
<Route path="/account/orders" element={<OrdersPage />} />
```

Find this list early. From any URL in the browser, it tells you which file to open.

### The folder map

Big frontends sort files in one of two ways:

| By type | By feature |
|---|---|
| `components/`, `hooks/`, `pages/`, `utils/` | `features/catalogue/`, `features/cart/`, `features/checkout/` |
| Easy to start with | Each feature's component, hook, API calls and tests sit together |
| Grows into folders of 200 files | Shared pieces live in `components/ui/` or `shared/` |

Most growing apps move from the first to the second. Spend ten minutes clicking through the folders, and sketch the map on paper: which features exist, and where the shared parts live.

### Follow one feature end to end

Pick something you can see, such as **Add to cart**, and follow it all the way through.

```animate
title: Tracing Add to cart through the codebase
kind: scene
nodes:
  - { id: card, label: "ProductCard.jsx", emoji: "🧸", row: 0, col: 0 }
  - { id: hook, label: "useCart.js", emoji: "🪝", row: 0, col: 2 }
  - { id: context, label: "CartProvider.jsx", emoji: "🧺", value: "2 items", row: 0, col: 4 }
  - { id: api, label: "api/cart.js", emoji: "📡", row: 2, col: 4 }
  - { id: header, label: "Header.jsx", emoji: "🔢", value: "Cart (2)", row: 2, col: 0 }
steps:
  - say: Start where the customer clicks. Search for the words "Add to cart" and you land in ProductCard.jsx.
    focus: [card]
  - say: Its onClick calls addItem from a hook. Go to the hook's definition with one key press in your editor.
    send: { from: card, to: hook, label: "addItem(product)" }
  - say: The hook reads the cart from a context, so the provider holds the real state. Find where the provider is.
    send: { from: hook, to: context, label: "dispatch add" }
    set: { context: "3 items" }
  - say: The provider also saves the cart to the server through a small API module.
    send: { from: context, to: api, label: "PUT /cart" }
  - say: Every component that reads the context re-renders, including the header's count. Now you know the whole path.
    send: { from: context, to: header, label: "new cart" }
    set: { header: "Cart (3)" }
```

One feature traced end to end teaches you the team's patterns: where state lives, how data is fetched, how errors are shown. Most other features follow the same patterns.

### Search like a professional

Your editor's search, and **ripgrep** (`rg`) on the command line, find things across thousands of files in a second. Three searches solve most "where is it?" questions:

- **The words the customer sees.** Search for `Out of stock` to find what draws it.
- **A name.** Search for `StockBadge` to find where a component is defined, and every place that uses it.
- **A URL or API path.** Search for `/api/stock` to find the code that talks to the server.

```callout jargon
**Go to definition** jumps from a name to where it's declared. **Find all references** lists every place a name is used. In VS Code they're F12 and Shift+F12, and they understand imports, so they beat plain text search for names.
```

When the words are in a translation file, search twice: first the words, to find their key, then the key, to find the component.

### React DevTools

```callout jargon
**React DevTools** is a browser extension that adds two tabs to DevTools. **Components** shows the tree of React components on the page, with each one's props, state and hooks. **Profiler** records which components rendered and how long they took.
```

It's the fastest way from "that thing on the screen" to "that file":

1. Open the Components tab and click the pick tool.
2. Click the "Out of stock" badge on the page.
3. DevTools selects `StockBadge`, shows its props (`stock: 0`), and lists its parents up to `App`.

Now you know the component's name, what it was given, and who rendered it. Search for that name, and you're in the right file.

### git log and git blame

The code tells you **what**. History tells you **why**.

```text
$ git log --oneline -- src/components/StockBadge.jsx
a41c9e2 Show "Only 2 left" when stock is low (WEB-0912)
7d02b11 Move stock badge into shared components for the admin app
3f8e6a0 Add stock badge to product cards
```

- **`git log -- <file>`** lists the commits that changed a file, newest first.
- **`git blame <file>`** shows, for each line, the last commit that changed it. Your editor can show this inline.

The commit `7d02b11` explains something important: the admin app uses this badge too. A commit message or PR often links a ticket with the full story.

### Shared components and conventions

Big teams build a **design system**: shared components like `Button`, `Badge` and `Dialog`, plus design tokens for colours and spacing. They often live in `components/ui/`, and may have a **Storybook**, a page that shows every shared component in every state.

Before you write a new component, look for an existing one. And notice the team's conventions: how files are named, how tests are written, how errors are shown. Copy the patterns around you, even when you'd have chosen differently. A codebase that does one thing three ways is harder for everyone.

Changing a shared component changes every screen that uses it. Find all its references first.

### Ask good questions

Getting stuck is normal. Staying stuck for a day is expensive. A good rule: try on your own for about 30 minutes, then ask. Make the question easy to answer:

```text
I'm changing the "Out of stock" badge for pre-orders (WEB-1008).
I found StockBadge in components/ui and the "stock.out" key in i18n/en.json.
StockBadge is also used in the admin Inventory page, so I don't want to change its default.
Is adding a `preorder` prop the team's usual way, or is there a pattern I've missed?
```

It says what you're doing, what you found, what you tried, and asks one specific question.

### Your first changes

Make your first changes small and safe:

- **Change as little as possible.** One prop, one line, one test.
- **Add a test first** that shows the new behaviour, in the style of the tests around it.
- **Run everything**: tests, lint and the build. Click through the preview.
- **Leave it a little better.** Fix the out-of-date README line you noticed on day one. Don't refactor a whole folder in your first week.

## Worked example

Here's the badge ticket, traced step by step.

**1. Search for the words.** `rg "Out of stock" src` finds only one hit: `src/i18n/en.json`, with the line `"stock.out": "Out of stock"`. The words are in a translation file.

**2. Search for the key.** `rg "stock.out" src` finds `src/components/ui/StockBadge.jsx`:

```jsx
export function StockBadge({ stock }) {
  if (stock === 0) return <Badge tone="grey">{t("stock.out")}</Badge>;
  if (stock <= 2) return <Badge tone="marigold">{t("stock.low", { count: stock })}</Badge>;
  return null;
}
```

**3. Find all references** to `StockBadge`: `features/catalogue/ProductCard.jsx` and `features/admin/Inventory.jsx`. The admin team wants "Out of stock" to stay. So changing what `stock === 0` shows would break their screen.

**4. Check the history.** `git log` on the file shows commit `7d02b11`, "Move stock badge into shared components for the admin app". That confirms it.

**5. Make the smallest change.** Add an optional prop, and pass it only from the catalogue:

```jsx
export function StockBadge({ stock, preorder = false }) {
  if (stock === 0) return <Badge tone="grey">{t(preorder ? "stock.preorder" : "stock.out")}</Badge>;
  // ...the rest unchanged
}
```

Add `"stock.preorder": "Back after Diwali"` to `en.json`, pass `preorder={product.preorder}` in `ProductCard.jsx`, and add one test for each case. The admin screen doesn't change, because the prop defaults to `false`.

## Try it

This program searches a tiny pretend codebase, the way `rg` or your editor would. Run it.

```js run
const files = {
  "src/features/catalogue/ProductCard.jsx": "<StockBadge stock={product.stock} />",
  "src/components/ui/StockBadge.jsx": 'return stock === 0 ? <Badge tone="grey">{t("stock.out")}</Badge> : null;',
  "src/i18n/en.json": '"stock.out": "Out of stock"',
  "src/features/admin/Inventory.jsx": "<StockBadge stock={row.stock} />",
};

const search = (text) => Object.keys(files).filter((path) => files[path].includes(text));

console.log(search("Out of stock").join(", "));
console.log(search("stock.out").join(", "));
console.log(search("StockBadge").join(", "));
```

It prints:

```text
src/i18n/en.json
src/components/ui/StockBadge.jsx, src/i18n/en.json
src/features/catalogue/ProductCard.jsx, src/features/admin/Inventory.jsx
```

The words lead to the key, the key leads to the component, and the component's name leads to everyone who uses it.

1. Why doesn't the search for `StockBadge` find `StockBadge.jsx` itself? What would you search for to find where it's defined?
2. Add a file `"src/features/cart/CartLine.jsx"` that uses `<StockBadge stock={line.stock} />`. Which screens would a change to the default now affect?

## Recap

- Don't read everything. Find how the app starts, where things live, and trace one feature end to end.
- Start from `package.json` (scripts and big dependencies), then `index.html`, `main.jsx`, `<App />` and the routes.
- Search for the words on screen, then for names and keys. Use go to definition and find all references for names.
- React DevTools takes you from a thing on the page to its component, props and parents. `git log` and `git blame` tell you why code is the way it is.
- Reuse shared components and follow the team's conventions. Ask after about 30 minutes stuck, with what you found and tried. Keep first changes small, tested and safe.

## Common mistakes

**1. Trying to read the whole codebase first.**

```text
Day 1: src/a... Day 3: src/f... Day 5: still reading, no PR yet
```

Symptom: a week later you've read 150 files, remember few of them, and haven't shipped anything. Fix: map the entry, routes and folders in an hour, trace one feature, then read only what your ticket needs.

**2. Changing a shared component's default.**

```jsx
if (stock === 0) return <Badge tone="grey">{t("stock.preorder")}</Badge>;
```

Symptom: the catalogue is right, but the admin Inventory page now says "Back after Diwali" for every empty shelf. Fix: find all references first, and add an optional prop that only the catalogue sets.

**3. Searching only for the words on screen.**

```text
$ rg "Out of stock" src
src/i18n/en.json
```

Symptom: you edit the translation file, and the words change on every screen that uses the key. Fix: search again for the key (`stock.out`) to find the component, and decide there.

**4. Writing a new component instead of finding the shared one.**

```jsx
function MyBadge({ text }) { return <span className="my-badge">{text}</span>; }
```

Symptom: the reviewer points out that `components/ui/Badge` already exists, with the brand colours and accessible contrast. Fix: look in the shared folder or Storybook before you build.

**5. Staying stuck for a day without asking.**

```text
"I didn't want to bother anyone."
```

Symptom: a question someone could answer in two minutes costs a day, and your ticket slips. Fix: after about 30 minutes, ask, with what you're doing, what you found and one specific question.
