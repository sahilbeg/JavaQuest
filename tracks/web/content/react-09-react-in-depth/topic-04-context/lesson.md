## Hook

When Meera Aunty changes the stall's opening time, she doesn't whisper it to Raju, who tells Arjun, who tells you. She posts it once in the team's WhatsApp group, and everyone who's in the group can read it.

React has a "group" like that. A component posts a value once, high up the page. Any component below it can read the value directly, without every component in between passing it along.

## Concept

### Prop drilling

In the store, the cart is needed by the header (for `Cart (3)`), by every Add button, and by the total. The Add buttons live deep inside the page:

```jsx
<Shop>
  <Header />
  <Main>
    <Shelf>
      <ProductCard>
        <AddButton />
```

To pass the cart down with props, `Main`, `Shelf` and `ProductCard` must all accept a `cart` prop and hand it on. None of them uses it.

```callout jargon
**Prop drilling** is passing a prop through components that don't need it, only so it reaches one that does. It's fine for one or two levels. Deeper, it makes every layer harder to change.
```

And calling `useCart()` in each component doesn't help. As the last ticket showed, each call gets its own cart.

### Syntax: createContext

```jsx
const CartContext = createContext(null);
```

```callout jargon
**Context** is a way for a component to make a value available to every component below it, however deep. `createContext` makes a new context. Its argument is the **default value**, used when no component above has provided one.
```

Call `createContext` once, at the top level of a file, and export it. Never call it inside a component, or every render makes a brand-new context.

### Syntax: providing a value

```jsx
<CartContext value={cart}>
  {children}
</CartContext>
```

```callout jargon
A **provider** is the component that puts a value into a context for everything inside it. Here the context itself is the provider.
```

In React 19 you render the context directly with a `value`. Older code, and most tutorials, write `<CartContext.Provider value={cart}>`. It means the same thing.

Most teams wrap the provider in a small component of its own, so the rest of the app never touches the context directly:

```jsx
export function CartProvider({ children }) {
  const cart = useCart();
  return <CartContext value={cart}>{children}</CartContext>;
}
```

`children` is whatever you put between `<CartProvider>` and `</CartProvider>`.

### Syntax: useContext

```jsx
const cart = useContext(CartContext);
```

`useContext` is a hook, so the usual rules apply. It returns the value from the nearest provider above this component. When that value changes, React renders this component again.

```jsx
function Header() {
  const { count } = useContext(CartContext);
  return <p>Cart ({count})</p>;
}
```

### How a component finds its cart

React looks upward from the component, through its parents, and stops at the first provider it finds.

```animate
title: One cart, read from anywhere below
kind: scene
nodes:
  - { id: provider, label: CartProvider, emoji: "🧺", value: "count 0", row: 0, col: 1 }
  - { id: header, label: Header, emoji: "🔝", value: "Cart (0)", row: 1, col: 0 }
  - { id: shelf, label: Shelf, emoji: "🗄️", row: 1, col: 2 }
  - { id: card, label: ProductCard, emoji: "🧶", row: 2, col: 2 }
  - { id: button, label: AddButton, emoji: "➕", row: 3, col: 2 }
links:
  - { from: provider, to: header }
  - { from: provider, to: shelf }
  - { from: shelf, to: card }
  - { from: card, to: button }
steps:
  - say: CartProvider calls useCart() once and puts that one cart into the context.
    focus: [provider]
  - say: Header calls useContext. React walks up the tree and finds the provider.
    send: { from: header, to: provider, label: useContext }
  - say: AddButton is three levels down. It reads the same cart directly. Shelf and ProductCard never see it.
    send: { from: button, to: provider, label: useContext }
  - say: A customer presses Add Keychain. The button calls add from the shared cart.
    send: { from: button, to: provider, label: "add(KEY)" }
    set: { provider: "count 1" }
  - say: The context's value changed, so React renders every component that reads it.
    send: { from: provider, to: header, label: "new value" }
    set: { header: "Cart (1)" }
```

Here is the same lookup in plain JavaScript. Each object points to its parent, and the search climbs until it finds a value:

```js run
function findCart(component) {
  let node = component;
  while (node !== null) {
    if (node.provides) return node.provides;
    node = node.parent;
  }
  return null;
}

const shop = { name: "Shop", provides: "the shop's cart", parent: null };
const shelf = { name: "Shelf", parent: shop };
const card = { name: "ProductCard", parent: shelf };
const giftShop = { name: "GiftShop", provides: "the gift cart", parent: shop };
const giftCard = { name: "ProductCard", parent: giftShop };
const footer = { name: "Footer", parent: null };

console.log(findCart(card));
console.log(findCart(giftCard));
console.log(findCart(footer));
```

- The card in the shelf finds the shop's cart.
- The card inside `GiftShop` stops at the nearer provider, so it gets the gift cart.
- The footer has no provider above it, so it gets `null`: the default value.

### Syntax: a hook that guards the context

```jsx
export function useCartContext() {
  const cart = useContext(CartContext);
  if (cart === null) {
    throw new Error("useCartContext must be used inside <CartProvider>");
  }
  return cart;
}
```

With a default of `null`, a component outside the provider would get `null` and crash with a confusing error, far from the real cause. This small custom hook turns that into a clear message. Components call `useCartContext()` and never import the context itself.

### Each provider makes its own cart

Every `CartProvider` on the page calls `useCart()` once, so each provider holds a separate cart. Components read from the nearest one. That's handy: a gift-wrapping panel could have its own little cart, separate from the main one.

### When to reach for context

Context is good for values that many components at different depths need: the cart, the logged-in customer, the colour theme, the language.

It has a cost. When the value changes, every component that reads it renders again. Don't move all your state into one big context. Keep state close to where it's used, and lift it to context only when props get painful.

```callout tip
Big apps often use a state library like **Redux Toolkit** or **Zustand** for shared state. They solve the same problem as context, with extra tools for large teams. Context plus a hook is plenty for Dhaaga's cart.
```

## Worked example

Here is the shared cart, and the three components that read it.

```jsx
import { createContext, useContext } from "react";
import { useCart } from "./useCart.js";

export const CartContext = createContext(null);

export function CartProvider({ children }) {
  const cart = useCart();
  return <CartContext value={cart}>{children}</CartContext>;
}

export function useCartContext() {
  const cart = useContext(CartContext);
  if (cart === null) {
    throw new Error("useCartContext must be used inside <CartProvider>");
  }
  return cart;
}
```

```jsx
export function Header() {
  const { count } = useCartContext();
  return <p>Cart ({count})</p>;
}

export function AddButton({ item }) {
  const { add } = useCartContext();
  return <button onClick={() => add(item)}>Add {item.name}</button>;
}

export function Shop() {
  return (
    <CartProvider>
      <Header />
      <Shelf />
    </CartProvider>
  );
}
```

- `CartContext` is made once, at the top of the file.
- `CartProvider` calls `useCart()` once. Everything inside it shares that one cart.
- `Header` and `AddButton` each take only what they need from the cart.
- `Shelf` and `ProductCard` have no cart props at all. Adding a new layer between them changes nothing.

## Try it

Open the preview and add a few things. The header and the total now agree.

Then, in `src/Shop.jsx`, move `<Header />` out of `<CartProvider>`, just above it, and wrap the two in a fragment, `<>...</>`. The preview goes blank, and the browser console shows your own message: `useCartContext must be used inside <CartProvider>`. Move it back.

## Recap

- Prop drilling means passing a prop through layers that don't use it. Context avoids it.
- `createContext(defaultValue)` makes a context. Call it once, outside components.
- A provider, `<CartContext value={cart}>`, shares a value with everything inside it.
- `useContext(CartContext)` reads the value from the nearest provider above, and re-renders when it changes.
- Wrap the context in a `CartProvider` component and a `useCartContext` hook, and throw a clear error when there's no provider.

## Common mistakes

**1. Reading the context outside its provider.**

```jsx
<>
  <Header />
  <CartProvider>
    <Shelf />
  </CartProvider>
</>
```

`Header` gets the default value, `null`. Destructuring `count` from `null` throws a `TypeError`, and the message doesn't mention the cart at all.

Fix: put every component that reads the cart inside `<CartProvider>`. A guard hook like `useCartContext` makes this mistake easy to spot.

**2. Forgetting the value.**

```jsx
return <CartContext>{children}</CartContext>;
```

Every component inside gets `undefined`, and `const { count } = useCartContext();` throws a `TypeError`.

Fix: `<CartContext value={cart}>{children}</CartContext>`.

**3. Making the context inside a component.**

```jsx
function CartProvider({ children }) {
  const CartContext = createContext(null);
  // ...
}
```

Each render makes a new context, so components that read the old one lose their value.

Fix: call `createContext` once, at the top level of the file, and export it.

**4. Calling useCart in each component instead of reading the context.**

```jsx
function Header() {
  const { count } = useCart();
  return <p>Cart ({count})</p>;
}
```

The header shows `Cart (0)` forever. It has a cart of its own, separate from the provider's.

Fix: `const { count } = useCartContext();`.
