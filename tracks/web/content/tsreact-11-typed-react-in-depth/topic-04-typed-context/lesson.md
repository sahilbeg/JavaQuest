## Hook

At the Aundh shop, Raju shouts "Asha Rao is here for her order!" once, and everyone hears it: the packer, the cashier, Meera Aunty at the back. He doesn't whisper it to the packer, who whispers it to the cashier, who whispers it to Meera.

React components usually pass data the whispering way, through props. This ticket shows the shouting way, called context, and how TypeScript keeps it safe.

## Concept

### Passing props through many layers

The header has the Sign in button. The greeting sits deep inside the page. To share the customer with props, every component in between must take a `customer` prop and pass it on, even ones that never use it.

```callout jargon
**Prop drilling** is passing a prop through components that don't need it, only so it reaches one far below. It works, but every layer in between grows a prop it doesn't care about.
```

```tsx
<Page customer={customer}>        {/* doesn't use it */}
  <Main customer={customer}>      {/* doesn't use it */}
    <Greeting customer={customer} />
  </Main>
</Page>
```

### Context: one value, many readers

```callout jargon
**Context** lets a component high up provide a value, and any component below read it directly, however deep. The component that provides it is the **provider**.
```

```animate
title: A provider shares the customer with the whole tree
kind: scene
nodes:
  - { id: provider, label: CustomerProvider, emoji: "📣", value: "customer: null", row: 0, col: 1 }
  - { id: header, label: AccountButton, emoji: "🔑", row: 1, col: 0 }
  - { id: main, label: Main, emoji: "📦", row: 1, col: 2 }
  - { id: greet, label: Greeting, emoji: "👋", value: "Sign in...", row: 2, col: 2 }
links:
  - { from: provider, to: header }
  - { from: provider, to: main }
  - { from: main, to: greet }
steps:
  - say: CustomerProvider sits at the top and holds the customer in state. Nobody has signed in yet.
    focus: [provider]
  - say: Asha taps Sign in. AccountButton reads signIn from the context and calls it.
    send: { from: header, to: provider, label: "signIn(Asha)" }
    set: { provider: "customer: Asha" }
  - say: The provider's value changed. React re-renders every component that reads this context.
    send: { from: provider, to: greet, label: "customer" }
    set: { greet: "Namaste, Asha!" }
  - say: Main never took a customer prop. The greeting read the context directly.
    focus: [main, greet]
```

### Syntax: creating a typed context

```tsx
const CustomerContext = createContext<CustomerContextValue | null>(null);
```

`createContext` takes a **default value**: what a component reads if there's no provider above it. TypeScript types the context from the type you give in `<...>`.

There's no sensible default customer, so the default is `null`, and the type says so: `CustomerContextValue | null`.

```tsx
interface CustomerContextValue {
  customer: Customer | null;
  signIn: (customer: Customer) => void;
  signOut: () => void;
}
```

The value holds the data and the functions that change it, so readers can do both.

### Syntax: providing the value

```tsx
<CustomerContext.Provider value={{ customer, signIn, signOut }}>
  {children}
</CustomerContext.Provider>
```

TypeScript checks `value` against the context's type. Forget `signOut`, and you see an error before anything runs:

```text
error TS2741: Property 'signOut' is missing in type '{ customer: Customer | null; signIn: (next: Customer) => void; }' but required in type 'CustomerContextValue'.
```

In React 19 you can also write `<CustomerContext value={...}>` without `.Provider`. Both do the same.

Usually a small component owns the state and the provider, so the rest of the app just wraps itself in it:

```tsx
export function CustomerProvider({ children }: { children: ReactNode }) {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const value = { customer, signIn: setCustomer, signOut: () => setCustomer(null) };
  return <CustomerContext.Provider value={value}>{children}</CustomerContext.Provider>;
}
```

### Syntax: reading it with useContext

```tsx
const value = useContext(CustomerContext);
```

`useContext` is a hook. It returns the value of the nearest provider above. Here its type is `CustomerContextValue | null`, so TypeScript won't let you write `value.customer` until you've ruled out `null`.

```text
error TS18047: 'value' is possibly 'null'.
```

### A hook that guards the null

Checking for `null` in every component gets old fast. So wrap `useContext` in a custom hook that checks once:

```tsx
export function useCustomer(): CustomerContextValue {
  const value = useContext(CustomerContext);
  if (value === null) throw new Error("useCustomer must be used inside <CustomerProvider>");
  return value;
}
```

After the `throw`, TypeScript knows `value` can't be `null`, so the hook returns a plain `CustomerContextValue`. A forgotten provider now fails loudly, with a message that says how to fix it.

```animate
title: Throwing narrows away null
kind: code
language: ts
code: |
  type Ctx = { name: string } | null;
  function need(value: Ctx): { name: string } {
    if (value === null) throw new Error("useCustomer must be used inside <CustomerProvider>");
    return value;
  }
  console.log(need({ name: "Asha" }).name);
  try { need(null); } catch (e) { console.log((e as Error).message); }
steps:
  - line: 2
    say: need takes a value that may be null, like useContext's answer.
  - line: 3
    say: If it is null, the function stops here with a clear error.
  - line: 4
    say: "Past the throw, null is impossible. TypeScript treats value as { name: string }."
  - line: 6
    out: "Asha"
    say: With a real value, we can read name straight away, no checks needed.
  - line: 7
    out: "useCustomer must be used inside <CustomerProvider>"
    say: With null, the error explains exactly what is missing.
```

```callout tip
Why not give `createContext` a fake default, like `{ customer: null, signIn: () => {}, signOut: () => {} }`? Then a missing provider fails silently: the Sign in button does nothing, and nobody knows why.
```

### Who re-renders when the value changes

Every component that calls `useContext(CustomerContext)` re-renders when the provider's `value` changes. React compares the old and new value with `Object.is`.

`value={{ customer, signIn, signOut }}` makes a new object on each render of the provider. That's fine for a small app. In a big one, the next tickets show how `useMemo` keeps the object the same until `customer` really changes.

## Worked example

Here's context for a different job: which shop a customer picked for pickup.

```tsx
import { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";

type Shop = "FC Road stall" | "Aundh shop";

interface ShopContextValue {
  shop: Shop;
  choose: (shop: Shop) => void;
}

const ShopContext = createContext<ShopContextValue | null>(null);

export function ShopProvider({ children }: { children: ReactNode }) {
  const [shop, setShop] = useState<Shop>("Aundh shop");
  return <ShopContext.Provider value={{ shop, choose: setShop }}>{children}</ShopContext.Provider>;
}

export function useShop(): ShopContextValue {
  const value = useContext(ShopContext);
  if (value === null) throw new Error("useShop must be used inside <ShopProvider>");
  return value;
}

export function PickupNote() {
  const { shop } = useShop();
  const hours = shop === "Aundh shop" ? "Tue–Sun 11:00–21:00" : "Sat–Sun 10:00–20:00";
  return <p>Pick up at the {shop}, open {hours}.</p>;
}
```

- `Shop` is a union of two literals, so `choose("Koregaon Park")` is a type error.
- `choose: setShop` works because `setShop` accepts a `Shop`, which fits `(shop: Shop) => void`.
- `PickupNote` can sit anywhere under `ShopProvider`, with no props at all.

## Try it

Open the preview. The greeting and the button are in different parts of the page. Press **Sign in as Asha** and watch the greeting change.

Then, in `main.tsx`, move `<Greeting />` outside `<CustomerProvider>`. The page shows your error message instead of a silent, broken greeting. Move it back.

## Recap

- Context shares a value with every component below a provider, without passing props through each layer.
- Type it with `createContext<Value | null>(null)` when there is no sensible default.
- Provide the data and the functions that change it in one value: `{ customer, signIn, signOut }`.
- Wrap `useContext` in a `useX` hook that throws when the value is `null`. Its return type is then never `null`.
- Every reader re-renders when the provider's value changes.

## Common mistakes

**1. Using the context without checking for null.**

```tsx
const { customer } = useContext(CustomerContext);
```

You see `Property 'customer' does not exist on type 'CustomerContextValue | null'`. TypeScript is right: without a provider, this would crash.

Fix: read it through `useCustomer()`, which throws a clear error and returns a non-null value.

**2. A fake default that hides a missing provider.**

```tsx
const CustomerContext = createContext<CustomerContextValue>({
  customer: null, signIn: () => {}, signOut: () => {},
});
```

Everything type-checks. But if someone forgets the provider, Sign in silently does nothing.

Fix: default to `null` and let `useCustomer` throw.

**3. Reading the context above its provider.**

```tsx
function App() {
  const { customer } = useCustomer();
  return <CustomerProvider>...</CustomerProvider>;
}
```

`App` throws `useCustomer must be used inside <CustomerProvider>`. A component only sees providers *above* it, not ones it renders.

Fix: put the provider higher, for example in `main.tsx`, around `<App />`.

**4. Forgetting a function in the value.**

```tsx
<CustomerContext.Provider value={{ customer, signIn }}>
```

You see `Property 'signOut' is missing in type ... but required in type 'CustomerContextValue'`.

Fix: provide everything the type promises: `{ customer, signIn, signOut }`.
