# The cascade and specificity: which rule wins

The sold-out baby booties look like any other product, because some rules beat the ones meant for them. Fix the fights without `!important`.

1. **TODO 1:** `#catalogue p` scores (1, 0, 1), so it beats `.stock` (0, 1, 0) and the Sold out label stays grey. Change the selector to `main p` (keep `color: #5a5a5a`).
2. **TODO 2:** delete `!important` from the `h3` rule, so `.sold-out h3` can grey out the booties' name.
3. **TODO 3:** delete the old `.buy { background-color: gray; }` rule at the bottom. It ties with the Marigold rule and wins because it comes later.
4. **TODO 4:** add `.sold-out .price` with `text-decoration: line-through`.

Don't add any id selectors or `!important`: the checks look for both.

File to change: `style.css`.
