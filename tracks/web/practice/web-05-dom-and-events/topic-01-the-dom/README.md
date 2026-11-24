# The DOM: the page as a tree of objects

Write a small page inspector in `src/inspect.js`. Each function gets an element and reads the tree around it.

1. **TODO 1:** `countProducts(list)` returns how many products the list holds. Count elements only, not the spaces between tags.
2. **TODO 2:** `productNames(list)` returns a real array of every product's name, in page order, trimmed.
3. **TODO 3:** `firstAndLast(list)` returns `{ first, last }` with the first and last names, trimmed, or `null` when the list is empty.
4. **TODO 4:** `parentId(element)` returns the `id` of the element that holds this one.

`src/main.js` already uses your functions to fill the summary line under the list. Open the preview to see it.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
