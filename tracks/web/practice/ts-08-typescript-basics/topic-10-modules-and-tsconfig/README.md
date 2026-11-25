# Typed modules and tsconfig

The price calculator is now split into modules. `types.ts` only holds types, and the other files import them with `import type`. Every import is already written. Fill in the five TODOs. Every amount is whole paise.

1. **TODO 1** in `src/money.ts`: `formatRupees(paise)` uses the `inr` formatter: `129900` gives `"₹1,299.00"`.
2. **TODO 2** in `src/prices.ts`: `priceOf(code)` returns the code's price from `PRICES`.
3. **TODO 3** in `src/quote.ts`: `quoteLine(line)` gives `"BAG x 2: ₹1,798.00"`, using `priceOf` and `formatRupees`.
4. **TODO 4** in `src/quote.ts`: `quoteTotal(lines)` adds up price times quantity for every line, in paise.
5. **TODO 5** in `src/config.ts`: set `TEAM_OPTIONS` to the team's settings: `strict: true`, `noEmit: true`, `target: "es2022"`, `module: "esnext"` and `moduleResolution: "bundler"`.

Press **Submit** to run the checks.
