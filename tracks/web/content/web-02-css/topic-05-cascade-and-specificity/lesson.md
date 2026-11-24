## Hook

Your mother says "wear a sweater". Your father says "it's warm, wear a T-shirt". Your grandmother says "wear the red kurta". Three people, three instructions, one body. Somebody's instruction has to win.

A web page has the same problem. One `<p>` can be picked by five different rules, and two of them may set its colour. The browser needs a fair way to choose. That system is the **cascade**, the C in CSS. Once you know it, "the page doesn't listen" stops being a mystery.

## Concept

### When rules disagree

Most of the time, rules don't fight. One rule sets a colour, another sets a font size, and the element gets both.

A fight happens when two rules set the **same property** on the **same element**. The browser then compares them in this order, and stops at the first step that has a winner:

1. **Importance:** a declaration marked `!important` beats normal ones.
2. **Specificity:** the more specific selector wins.
3. **Source order:** if it's still a tie, the rule that comes later wins.

```callout jargon
The **cascade** is this set of steps the browser uses to pick one value when several rules set the same property on an element.
```

### Syntax: specificity scores

```css
p             /* 0 ids, 0 classes, 1 type  -> (0, 0, 1) */
.stock        /* 0 ids, 1 class,   0 types -> (0, 1, 0) */
main p        /* 0 ids, 0 classes, 2 types -> (0, 0, 2) */
.sold-out h3  /* 0 ids, 1 class,   1 type  -> (0, 1, 1) */
#catalogue p  /* 1 id,  0 classes, 1 type  -> (1, 0, 1) */
```

**Specificity** is a score for how precisely a selector points at an element. Count three things in the selector:

1. ids (`#catalogue`),
2. classes (`.stock`) and pseudo-classes like `:hover`,
3. types, meaning tag names (`p`, `h3`).

Compare scores column by column, from the left. The first column that differs decides. So one id beats any number of classes, and one class beats any number of tags.

```callout analogy
It's like comparing exam marks subject by subject in a fixed order. If one student has more marks in the first subject, they win. The other subjects aren't even looked at.
```

Watch the fight over the Sold out label:

```animate
title: Who colours the Sold out label?
kind: scene
nodes:
  - { id: label, label: "<p class=\"stock\">", emoji: "🏷️", value: "?" }
  - { id: idrule, label: "#catalogue p", emoji: "⚫", value: "(1, 0, 1)", row: 1, col: 0 }
  - { id: classrule, label: ".stock", emoji: "🔴", value: "(0, 1, 0)", row: 1, col: 2 }
steps:
  - say: Two rules want to colour the Sold out label. One says grey, one says red.
    focus: [label, idrule, classrule]
  - say: Neither uses !important, so the browser compares specificity, starting with the ids column.
    focus: [idrule, classrule]
  - say: "#catalogue p has one id. .stock has none. 1 beats 0, so the fight is over right there."
    focus: [idrule]
  - say: The label turns grey. The class column never gets compared, even though .stock is the rule written for this label.
    set: { label: grey }
  - say: "Change #catalogue p to main p. Its score drops to (0, 0, 2), with no id at all."
    set: { idrule: "(0, 0, 2)" }
  - say: Now the ids tie at 0, and the classes column decides. .stock has 1, main p has 0. Red wins.
    set: { label: red }
    focus: [classrule, label]
```

### Source order breaks ties

```css
.buy {
  background-color: #f2a81d;
}

.buy {
  background-color: gray;
}
```

These two selectors are the same, so their specificity ties. The browser then takes the one written **later** in the file. The buttons are grey.

This is why old, forgotten rules at the bottom of a stylesheet cause surprises. Delete rules you no longer need.

### Inheritance

Some properties pass down from an element to everything inside it. This is called **inheritance**. Text properties do: `color`, `font-family`, `font-size` and `line-height`. Box properties don't: `padding`, `border`, `margin` and `background-color`.

```css
body {
  color: #2e3a8c;
}
```

This one rule makes every paragraph, list and heading Indigo, unless a rule picks them directly.

```callout warning
An inherited value is the weakest of all. Any rule that picks the element itself beats it, even a plain `p` with score (0, 0, 1).
```

```animate
title: Indigo flows down the page
kind: scene
nodes:
  - { id: body, label: "<body>", emoji: "📄", value: Indigo }
  - { id: main, label: "<main>", emoji: "📦", value: "-", row: 1, col: 0 }
  - { id: h3, label: "<h3>", emoji: "🔠", value: "-", row: 2, col: 0 }
  - { id: code, label: "<p class=\"code\">", emoji: "🔢", value: "-", row: 2, col: 2 }
links:
  - { from: body, to: main }
  - { from: main, to: h3 }
  - { from: main, to: code }
steps:
  - say: The body rule sets color to Indigo. No rule picks main, so main inherits Indigo from body.
    set: { main: Indigo }
    focus: [body, main]
  - say: No rule picks the h3's colour either, so it inherits Indigo from main.
    set: { h3: Indigo }
    focus: [h3]
  - say: The code paragraph would inherit Indigo too, but .code picks it directly and sets grey.
    focus: [code]
  - say: A rule aimed at the element always beats an inherited value, so the code line is grey.
    set: { code: grey }
```

### Syntax: !important

```css
h3 {
  color: #2e3a8c !important;
}
```

`!important` after a value makes that declaration beat every normal one, whatever the specificity. It sounds handy, but it's a trap.

Once a value is `!important`, the only way to beat it is another `!important`. Soon every rule shouts, and nothing can be changed safely. Fix the selectors instead.

```callout tip
When a rule "doesn't work", open DevTools, inspect the element, and look at the Styles panel. A beaten declaration is shown crossed out, and the winning rule sits above it.
```

## Worked example

Here is the part of the catalogue's stylesheet that hides the sold-out booties:

```css
#catalogue p {
  color: #5a5a5a;
}

.stock {
  color: #b3261e;
}

h3 {
  color: #2e3a8c !important;
}

.sold-out h3 {
  color: #5a5a5a;
}
```

1. The Sold out label is a `<p class="stock">` inside `<main id="catalogue">`. Both of the first two rules pick it.
2. `#catalogue p` scores (1, 0, 1) and `.stock` scores (0, 1, 0). The id wins, so the label is grey, not red.
3. The fix is to make the first selector weaker: `main p` scores (0, 0, 2), so `.stock` wins.
4. The booties' name should turn grey, and `.sold-out h3` (0, 1, 1) is more specific than `h3` (0, 0, 1). But `!important` jumps the queue, so the name stays Indigo.
5. The fix is to delete `!important`. Then specificity decides, and the name turns grey.

## Try it

Paste these two rules at the end of `style.css`:

```css
.product h3 {
  color: #f2a81d;
}

h3 {
  color: #b3261e;
}
```

Which colour do the product names get, Marigold or red? Work out both scores first: `.product h3` is (0, 1, 1) and `h3` is (0, 0, 1). Then check the preview. Swap the order of the two rules. Does anything change? Why not?

## Recap

- When rules set the same property on the same element, the cascade picks one: importance, then specificity, then source order.
- Specificity counts ids, then classes, then types, compared from the left. One id beats any number of classes.
- When specificity ties, the rule written later wins.
- Text properties like `color` and `font-family` are inherited, but any rule aimed at the element beats an inherited value.
- Avoid `!important` and ids in selectors. Use classes, and fix the selectors instead.

## Common mistakes

**1. Adding !important to win a fight.**

```css
.stock {
  color: #b3261e !important;
}
```

The label turns red today. Next month someone needs a different colour for one label, and has to add a second `!important`. Lower the specificity of the rule that's winning instead.

**2. Styling with ids.**

```css
#catalogue p {
  color: #5a5a5a;
}
```

The id gives this rule a score of (1, 0, 1). No class-based rule can ever override it. Use a type or a class: `main p` or `.product p`.

**3. Forgetting an old rule further down.**

```css
.buy {
  background-color: #f2a81d;
}
/* ...200 lines later... */
.buy {
  background-color: gray;
}
```

The buttons are grey, and the Marigold rule looks broken. The later rule wins the tie. Search the file for the selector, and delete the old rule.

**4. Expecting padding to be inherited.**

```css
.product {
  padding: 1rem;
}
```

The card gets padding, but the `<h3>` and `<p>` inside don't get any of their own. Only text properties are inherited. Box properties stay on the element you set them on.
