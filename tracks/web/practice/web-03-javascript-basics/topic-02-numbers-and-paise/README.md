# Numbers, maths and why prices are in paise

Fix the paise maths in `src/paise.js`. Every amount is whole paise: ₹1 is 100 paise.

1. **TODO 1:** `lineTotal(paise, qty)` returns the price of one item times the quantity.
2. **TODO 2:** `wholeRupees(paise)` returns the whole rupees, rounded down: `15050` gives `150`.
3. **TODO 3:** `leftoverPaise(paise)` returns the paise left over after the whole rupees: `15050` gives `50`.
4. **TODO 4:** `shareEach(totalPaise, people)` splits a bill equally, rounded down to whole paise: `100000` among `3` gives `33333`.

Press **Run** to see what `main.js` prints. Press **Submit** to run the checks.
