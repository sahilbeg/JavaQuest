# INC-03: The cart total is off by a few paise

The old calculator in `src/cart.js` keeps money in rupee decimals, and big orders come out a paisa short. Make it work in whole paise.

1. **Bronze:** `PRICES` holds every price in whole paise (`CST: 15000`). `toPaise(rupees)` rounds to the nearest paisa: `2068.2` gives `206820`. `cartTotal(lines)` adds up whole paise and takes off the Diwali offer: Mrs. Kapoor's two tote bags, baby booties and coaster set come to `206820`.
2. **Silver:** `diwaliDiscount(subtotalPaise)` gives 10% off only above `150000`, rounded down: `150000` gives `0`, and `150019` gives `15001`.
3. **Gold:** `formatRupees(paise)` uses `Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" })`: `206820` gives `"₹2,068.20"`, so `totalLine` reads `"Total: ₹2,068.20"`.

Press **Run** to see what `main.js` prints. Press **Submit** to run the checks.
