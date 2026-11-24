# Many requests at once: Promise.all

The stall's tablet asks for stock one item at a time. Make it ask for every item at once. Change `src/stockAll.js`. `getStock(id)` is already written: it returns one item's stock, and throws when the answer isn't ok.

**`stockForAll(ids)`**

1. **TODO 1:** start every request at once with `ids.map((id) => getStock(id))`, and wait for all of them with `Promise.all`.
2. **TODO 2:** return a list of `{ id, stock }` in the same order as `ids`, for example `[{ id: "DC-1042", stock: 3 }, { id: "DC-2001", stock: 12 }]`. If any request fails, `stockForAll` rejects.

**`stockReport(ids)`**

3. **TODO 3:** start every request at once and wait for them with `Promise.allSettled`.
4. **TODO 4:** return `{ found, failed }`: `found` holds `{ id, stock }` for every request that worked, and `failed` holds the ids of the ones that didn't, both in the order of `ids`. `stockReport` never rejects.

Press **Submit** in Cadence, or save the file in your editor. The checks use a pretend slow server where every answer takes one second, so asking one by one is too slow to pass.
