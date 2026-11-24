# Testing components like a user

dhaaga.in launches next week, and `ProductCard` has no tests. Your job is `src/productCardChecks.jsx`: checks that pass on today's card and fail the moment someone breaks it.

`ProductCard.jsx` already works. Don't change it. It takes two props:

- `product`: `{ id, name, paise, stock }`. The checks use `GOLU`: `DC-1042`, Golu the elephant, 45000 paise, 7 in stock.
- `onAdd(id, quantity)`: called when the customer presses **Add to cart**.

It shows the name, the price in rupees, a **Quantity** select (1 to 5, never more than the stock) and an **Add to cart** button. When `stock` is 0 it shows **Out of stock** instead of the select, and the button is disabled.

Each check is an `async` function that takes a component, `Card`, renders it with `render(<Card product={...} onAdd={...} />)`, and uses `expect`. `checkShowsPrice` is a worked example to copy.

1. **TODO 1:** `checkAddsOne`. Click **Add to cart** once and check that `onAdd` was called exactly once, with `"DC-1042"` and `1`.
2. **TODO 2:** `checkAddsChosenQuantity`. Pick `3` in the select labelled **Quantity**, click **Add to cart**, and check the call was `("DC-1042", 3)`. Find the select by its label, not by its tag or a class.
3. **TODO 3:** `checkSoldOut`. Render Golu with `stock: 0`. Check the words **Out of stock**, a disabled button, and that clicking it adds nothing.

Use `vi.fn()` for `onAdd`, `userEvent.setup()` for clicks and choices, and `screen` queries by role and label.

## How your checks are graded

First, every check runs on the real `ProductCard`. All of them must pass.

Then your checks run on six broken copies of the card. Each copy has one bug:

- it shows paise as rupees (`₹45000.00`)
- it sends the product's name instead of its id
- it calls `onAdd` twice on one click
- it always adds 1, whatever quantity you pick
- its Quantity select has no label
- a sold-out card still has a working button

At least one of your checks must fail on each broken copy. The first one is already caught by the worked example.
