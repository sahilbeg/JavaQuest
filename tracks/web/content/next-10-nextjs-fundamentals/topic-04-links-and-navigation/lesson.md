## Hook

Moving to another room in your house doesn't mean leaving the house, walking round the block and coming back in. You just walk through the door. The furniture in the hall stays where it was.

A plain `<a>` link on the new site leaves the house every time. The browser throws the whole page away and loads a new one, so the screen flashes white and the layout starts over. Next.js has a link that walks through the door.

## Concept

### Syntax: Link

```jsx
import Link from "next/link";

<Link href="/items">Shop</Link>
```

`Link` comes from `next/link`. It takes an `href`, like an `<a>`, and it renders a real `<a href="/items">` into the page. So it still works as a link: it can be opened in a new tab, copied, and read out by screen readers.

The difference is what happens on a tap.

### A plain <a> versus Link

```callout jargon
A **full page load** throws away the current page, asks the server for a new HTML document, and starts all JavaScript again. **Client-side navigation** keeps the page loaded and swaps only the part that changed.
```

```animate
title: Tapping Shop, two ways
kind: scene
nodes:
  - { id: phone, label: Phone, emoji: "📱", value: "Home, cart: 2" }
  - { id: next, label: Next.js server, emoji: "🖥️" }
steps:
  - say: With a plain <a href="/items">, the browser forgets the page it has. The screen goes white.
    set: { phone: white screen }
  - say: It asks the server for a whole new HTML document, then downloads the JavaScript and runs it again.
    send: { from: phone, to: next, label: "GET /items (full page)" }
  - say: The new page arrives. Every layout started over, so state in the header, like the cart count, is lost.
    send: { from: next, to: phone, label: "whole HTML document", back: true }
    set: { phone: "Shop, cart: 0" }
  - say: Now with Link. Back on Home, the customer taps Shop again.
    set: { phone: "Home, cart: 2" }
  - say: Link stops the browser's own navigation. Next.js asks the server only for the new page's part.
    send: { from: phone, to: next, label: "just the Shop page" }
  - say: It swaps that part in. The layout never left, so the header and the cart count stay. No white flash.
    send: { from: next, to: phone, label: "Shop page", back: true }
    set: { phone: "Shop, cart: 2" }
```

`Link` also **prefetches**: when a link comes into view, Next.js quietly fetches what that page needs, ahead of the tap. In a production build, most taps then feel instant.

```callout tip
Use `Link` for every address inside your own site. Keep a plain `<a>` for other websites, like a link to Instagram.
```

### Showing which page you're on

A menu should say which page is open. The accessible way is the `aria-current` attribute:

```jsx
<a href="/items" aria-current="page">Shop</a>
```

```callout jargon
**aria-current="page"** tells screen readers "this link is the page you're on". CSS can style it too, with the selector `[aria-current="page"]`. A link that isn't current simply leaves the attribute out.
```

To set it, the menu needs the current address.

### Syntax: usePathname

```jsx
"use client";
import { usePathname } from "next/navigation";

const pathname = usePathname();
```

`usePathname()` returns the path part of the current address, like `"/items/DC-1042"`. It has no `?` part and no domain. When the customer navigates, the component renders again with the new path.

It's a hook, and hooks only work in a **client component**: the kind of component that also runs in the browser. The line `"use client"` at the very top of the file makes one. Ticket 5 explains it properly. For now: a component that uses `usePathname` or `useRouter` starts with `"use client"`.

### Which link is active?

`"/items"` should count as active on `/items` and also on `/items/DC-1042`, which is inside the shop. But `"/"` can't use the same rule, because every address starts with `/`. The home link is active only on `/` itself.

```js run
const pathname = "/items/DC-1042";
console.log(pathname === "/items");
console.log(pathname.startsWith("/items/"));
console.log(pathname.startsWith("/"));
```

Checking for `href + "/"` matters: `"/itemsale".startsWith("/items")` is `true`, but it isn't inside the shop.

### Syntax: useRouter

```jsx
"use client";
import { useRouter } from "next/navigation";

const router = useRouter();
router.push("/items?q=golu");
```

Sometimes navigation comes from code, not from a link: after a search form is sent, for example. `useRouter()` returns a router with methods:

- `router.push(href)` goes to a new address, adding it to the history, so Back returns.
- `router.replace(href)` goes there without adding history.
- `router.back()` is like pressing Back.

Import it from `next/navigation`. There's an older `next/router` for the pages router, which doesn't work in the app router.

### Putting text in an address

A search for `Golu elephant` goes to `/items?q=Golu%20elephant`. Spaces and symbols like `&` and `?` mean something in an address, so they have to be **encoded** first.

```js run
console.log(encodeURIComponent("golu"));
console.log(encodeURIComponent("Golu elephant"));
console.log("/items?q=" + encodeURIComponent("rani & golu"));
```

`encodeURIComponent` turns a space into `%20` and `&` into `%26`, so the address can't be misread.

### Syntax: redirect on the server

```jsx
import { redirect } from "next/navigation";

redirect("/items");
```

Inside a server component, `redirect(href)` stops the page and sends the customer to another address instead. It works by throwing a special error that Next.js catches, so nothing after it runs. Use it when an address has moved, like an old `/catalogue` page that should now go to `/items`.

## Worked example

Here's a footer menu with an active link, built the same way as this ticket's `NavBar`:

```jsx
"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";

const HELP_LINKS = [
  { href: "/help/delivery", label: "Delivery" },
  { href: "/help/returns", label: "Returns" },
];

export function HelpMenu() {
  const pathname = usePathname();
  return (
    <ul>
      {HELP_LINKS.map((link) => (
        <li key={link.href}>
          <Link href={link.href} aria-current={pathname === link.href ? "page" : undefined}>
            {link.label}
          </Link>
        </li>
      ))}
    </ul>
  );
}
```

- `"use client"` comes first, because the component uses a hook.
- The links come from an array, drawn with `map` and a `key`, as in react-08.
- `Link` passes `aria-current` on to the `<a>` it renders. `undefined` means the attribute is left out.
- On `/help/returns`, only Returns gets `aria-current="page"`.

## Try it

Run the snippets above with other paths. Try `"/itemsale"` with `startsWith("/items/")` and with `startsWith("/items")`. Only the first gives the answer a menu needs.

## Recap

- `Link` from `next/link` renders a real `<a>`, but navigates without a full page load, keeping layouts and their state.
- Mark the current page's link with `aria-current="page"`, using `usePathname()` to know where you are.
- `useRouter().push(href)` navigates from code, like after a search. Encode text with `encodeURIComponent`.
- Hooks like `usePathname` and `useRouter` come from `next/navigation`, and need `"use client"` at the top of the file.
- `redirect(href)` sends the customer elsewhere from a server component.

## Common mistakes

**1. Plain <a> tags for your own pages.**

```jsx
<a href="/items">Shop</a>
```

It works, but every tap is a full page load: a white flash, and the header's state starts over.

Fix: `<Link href="/items">Shop</Link>`.

**2. Putting an <a> inside Link.**

```jsx
<Link href="/items">
  <a>Shop</a>
</Link>
```

That was how very old versions of Next.js worked. Now `Link` already renders an `<a>`, so this makes a link inside a link, and Next.js shows an error about an invalid `<Link>` with an `<a>` child.

Fix: put the text straight inside: `<Link href="/items">Shop</Link>`.

**3. Importing the router from the wrong place.**

```jsx
import { useRouter } from "next/router";
```

In the app router, the page fails with an error saying NextRouter was not mounted. `next/router` belongs to the old pages router.

Fix: `import { useRouter } from "next/navigation";`.

**4. Using a hook without "use client".**

```jsx
import { usePathname } from "next/navigation";

export function NavBar() {
  const pathname = usePathname();
  // ...
}
```

Next.js stops with an error saying this hook only works in a client component.

Fix: add `"use client";` as the very first line of the file.

**5. Marking Home as active everywhere.**

```jsx
aria-current={pathname.startsWith(link.href) ? "page" : undefined}
```

Every address starts with `/`, so Home is marked as the current page on every page.

Fix: treat `"/"` as active only when `pathname === "/"`.
