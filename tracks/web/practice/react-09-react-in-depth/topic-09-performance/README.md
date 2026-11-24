# Performance: memo, useMemo and useCallback

Stop the catalogue doing wasted work on every tap of Add.

The page already works. `ProductRow` writes its item code into `rowRenders` each time it renders, and `sortByPrice` counts its runs in `sortStats`, so the checks can count the work. Don't change those files' counting.

1. **TODO 1** in `src/ProductRow.jsx`: wrap `ProductRow` in `memo`, so it skips rendering when its props are the same. Keep the name `ProductRow` for the export.
2. **TODO 2** in `src/Catalogue.jsx`: keep the sorted list with `useMemo`, so it is sorted again only when `items` or `order` change.
3. **TODO 3** in `src/Catalogue.jsx`: keep `handleAdd` the same function between renders with `useCallback`.

After your change, a tap on Add should render no product row again and run no sort, while the cart count still goes up.
