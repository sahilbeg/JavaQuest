# Performance with types

Stop the order history doing wasted work on every tap of **Open**.

The page already works. `OrderRow` adds its order id to `rowRenders` each time it renders, and `filterOrders` counts its runs in `filterStats`, so the checks can count the work. Don't change that counting.

1. **TODO 1** in `src/OrderRow.tsx`: wrap `OrderRow` in `memo`, so it skips rendering when its props are the same. Keep the export name `OrderRow` and its `OrderRowProps` type.
2. **TODO 2** in `src/OrderHistory.tsx`: keep the search result with `useMemo`, so `filterOrders` runs again only when `orders` or `query` change.
3. **TODO 3** in `src/OrderHistory.tsx`: keep `handleOpen` the same function between renders with `useCallback`.

After your change, a tap on Open shows `Opened order-17` but renders no row again and runs no search. Typing in **Search orders** still searches once per letter.
