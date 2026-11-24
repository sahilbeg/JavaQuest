## Hook

At the stall, there is a pad of blank price tags. Each tag has the same shape: a line for the name, a line for the price and a line for the colour. You fill in different words for each item.

A component with props works the same way. The component is the blank tag. Props are the words you write on it.

## Concept

### Props: a component's inputs

So far every component showed the same thing every time. A `ProductCard` that always says "Golu the elephant" is no use for the tote bag.

```callout jargon
**Props** (short for properties) are the values you pass into a component, like arguments to a function. The component uses them to decide what to show.
```

### Syntax: passing props

```jsx
<ProductCard name="Golu the elephant" paise={45000} colour="Indigo" />
```

Props look like HTML attributes. Text goes in quotes. Any other value, such as a number, goes in curly braces. `paise={45000}` passes the number 45000, while `paise="45000"` would pass the text `"45000"`.

### Syntax: reading props

```jsx
function ProductCard(props) {
  return <h3>{props.name}</h3>;
}
```

React collects all the props into one object and passes it as the first argument. Here `props` is `{ name: "Golu the elephant", paise: 45000, colour: "Indigo" }`.

### Syntax: destructuring props

```jsx
function ProductCard({ name, paise, colour }) {
  return (
    <article className="product-card">
      <h3>{name}</h3>
      <p className="price">{rupees(paise)}</p>
      <p>Colour: {colour}</p>
    </article>
  );
}
```

Most React code unpacks the object right in the brackets. This is the destructuring you learned for objects. Now `name` is a plain variable, and the first line of the function tells you which props it uses.

`rupees` is a small helper that turns paise into text like `₹450.00`. You wrote one like it when you learned to format money.

### Syntax: a default value

```jsx
function ProductCard({ name, paise, colour = "Off-white" }) {
```

If the parent leaves out `colour`, it is `"Off-white"`. If the parent passes a colour, that one wins.

Watch a plain function do the same unpacking:

```animate
title: One object in, three variables out
kind: code
language: js
code: |
  function productCard({ name, paise, colour = "Off-white" }) {
    return `${name} | ₹${paise / 100} | ${colour}`;
  }
  console.log(productCard({ name: "Golu", paise: 45000, colour: "Indigo" }));
  console.log(productCard({ name: "Coaster set", paise: 15000 }));
steps:
  - line: 4
    say: The caller packs three values into one object and passes it. React does this with your props.
  - line: 1
    vars: { name: '"Golu"', paise: 45000, colour: '"Indigo"' }
    say: The braces unpack the object into three variables. A colour was given, so the default is not used.
  - line: 2
    say: The function builds its result from those three values.
  - line: 4
    out: "Golu | ₹450 | Indigo"
    say: The first call prints Golu's line.
  - line: 5
    say: The second object has no colour at all.
  - line: 1
    vars: { name: '"Coaster set"', paise: 15000, colour: '"Off-white"' }
    say: colour is missing, so it takes the default, "Off-white".
  - line: 5
    out: "Coaster set | ₹150 | Off-white"
    say: Same function, different inputs, different result. That is all props are.
```

### Props flow down, and are read-only

A parent passes props to its children. The children read them and never change them.

```animate
title: Props flow one way
kind: scene
nodes:
  - { id: parent, label: "FeaturedProducts", emoji: "🧩", row: 0, col: 1 }
  - { id: c1, label: "ProductCard", emoji: "🐘", row: 1, col: 0 }
  - { id: c2, label: "ProductCard", emoji: "☕", row: 1, col: 1 }
  - { id: c3, label: "ProductCard", emoji: "👜", row: 1, col: 2 }
steps:
  - say: FeaturedProducts knows which three products to show this week.
    focus: [parent]
  - say: It passes Golu's name and price down to the first card.
    send: { from: parent, to: c1, label: "paise={45000}" }
    set: { c1: "₹450.00" }
  - say: The same component gets different props for the coaster set.
    send: { from: parent, to: c2, label: "paise={15000}" }
    set: { c2: "₹150.00" }
  - say: And again for the tote bag. One component, three different cards.
    send: { from: parent, to: c3, label: "paise={89900}" }
    set: { c3: "₹899.00" }
  - say: The cards only read their props. To show a new price, the parent passes a new one, and the card re-renders.
    focus: [c3]
```

```callout jargon
**Read-only** means you may look at a value but not change it. Props are read-only: a component never assigns to `props.paise` or to `name`.
```

```callout jargon
**One-way data flow** means data moves down from parent to child, never back up by itself. It makes bugs easier to find: if a card shows the wrong price, look at the parent that passed it.
```

### Syntax: the children prop

```jsx
function Badge({ children }) {
  return <span className="badge">{children}</span>;
}

<Badge>Only 2 left</Badge>
```

Whatever you put between the opening and closing tags arrives as a prop called `children`. Here `children` is the text `"Only 2 left"`. It can also be other elements or components.

### Any value can be a prop

```jsx
<ProductCard
  name="Indigo tote bag"
  paise={89900}
  onSale={true}
  tags={["gift", "Diwali"]}
/>
```

Numbers, true or false, arrays and objects all go in braces. Writing just `onSale` with no value means `onSale={true}`. Functions can be props too. The events ticket uses that a lot.

## Worked example

Both Dhaaga places get a card on the "Visit us" page. One component, two uses:

```jsx
function Badge({ children }) {
  return <span className="badge">{children}</span>;
}

function PlaceCard({ name, days, hours, note = "Walk in any time" }) {
  return (
    <article className="place-card">
      <h3>{name}</h3>
      <p>{days}, {hours}</p>
      <Badge>{note}</Badge>
    </article>
  );
}

export function VisitUs() {
  return (
    <section>
      <PlaceCard name="FC Road stall" days="Sat and Sun" hours="10:00 to 20:00" />
      <PlaceCard name="Aundh shop" days="Tue to Sun" hours="11:00 to 21:00" note="Gift wrap at the counter" />
    </section>
  );
}
```

Line by line:

- `PlaceCard` unpacks four props. `note` has a default.
- `<p>{days}, {hours}</p>` mixes two values with a comma of plain text.
- `<Badge>{note}</Badge>` passes the note as the badge's `children`.
- The stall card leaves out `note`, so its badge says `Walk in any time`. The shop card passes its own note.

## Try it

A component is a function that receives one object. Run this. Then add a third call for the Koregaon Park pop-up, open `Diwali week only`, from `10:00 to 22:00`.

```js run
function PlaceCard(props) {
  return `${props.name}: ${props.days}, ${props.hours}`;
}

console.log(PlaceCard({ name: "FC Road stall", days: "Sat and Sun", hours: "10:00 to 20:00" }));
console.log(PlaceCard({ name: "Aundh shop", days: "Tue to Sun", hours: "11:00 to 21:00" }));
```

## Recap

- Props are a component's inputs. You pass them like attributes: text in quotes, everything else in braces.
- React passes all props as one object. Unpack it in the brackets: `function ProductCard({ name, paise })`.
- A default value, like `colour = "Off-white"`, is used when the parent leaves that prop out.
- Whatever sits between a component's tags arrives as the `children` prop.
- Props flow down from parent to child and are read-only.

## Common mistakes

**1. Passing a number in quotes.**

```jsx
function QuantityNote({ qty }) {
  return <p>With one more: {qty + 1}</p>;
}

<QuantityNote qty="2" />
```

The page says `With one more: 21`. The quotes made `qty` the text `"2"`, and `+` joined the texts. Pass numbers in braces: `qty={2}`.

**2. Forgetting the braces around the props.**

```jsx
function ProductCard(name, paise) {
  return <h3>{name}</h3>;
}
```

React stops with an error saying objects are not valid as a React child. Without braces, `name` is the whole props object, and React cannot draw an object. Write `function ProductCard({ name, paise })`.

**3. Changing a prop.**

```jsx
function ProductCard({ paise }) {
  paise = paise - 1000;
  return <p>{rupees(paise)}</p>;
}
```

This shows a price ₹10 lower than the parent passed. The card and the parent now disagree about the price, which is exactly the bug React is meant to stop. Leave props alone. Work out a new value in a new variable, such as `const salePaise = paise - 1000;`, or ask the parent to pass the right price.

**4. Misspelling a prop name.**

```jsx
<ProductCard nmae="Golu the elephant" paise={45000} />
```

No error appears, but the heading is empty. The card reads `name`, and nobody passed it. If a value is missing on the page, check the spelling where you pass it.
