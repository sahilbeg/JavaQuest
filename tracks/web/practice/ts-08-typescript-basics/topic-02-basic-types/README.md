# Basic types and inference

`src/basics.ts` is the price calculator's first TypeScript file. Every function already has its types, and it type-checks, but each one returns a placeholder. Fill them in. Every amount is whole paise: ₹1 is 100 paise.

1. **TODO 1:** `toPaise(rupees)` rounds to the nearest whole paisa: `450` gives `45000`, and `0.29` gives `29`.
2. **TODO 2:** `parseQty(typed)` turns typed text into a quantity, ignoring spaces around it: `" 2 "` gives `2`. Text that isn't a whole number of 0 or more (`"two"`, `"1.5"`, `"-1"`) gives `0`.
3. **TODO 3:** `rupeesText(paise)` gives `"₹"` and the rupees with two decimals: `45000` gives `"₹450.00"`.
4. **TODO 4:** `canMakeToys(grams, toys)` is `true` when there's enough yarn, at `TOY_GRAMS` (40 g) per toy.
5. **TODO 5:** `stockLabel(name, left)` gives `"Keychain: 3 left"`, or `"Keychain: sold out"` when `left` is `0`.

Keep the types as they are. Your code is type-checked before the checks run, so a type error shows up like a compile error, with its line. Press **Submit** to run the checks.
