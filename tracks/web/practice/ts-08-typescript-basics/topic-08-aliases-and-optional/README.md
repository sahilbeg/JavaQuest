# Type aliases and optional values

`src/orders.ts` names its types with `type`: an `OrderLine` may have `giftWrap` and `name`, and a `Customer` may have a `phone` and an `address`. Fill in the five functions. Every amount is whole paise.

1. **TODO 1:** `extrasFor(line)`: per item, `GIFT_WRAP` (₹30) when `giftWrap` is `true`, and `NAME_STITCHED` (₹50) when there's a non-empty `name`. Multiply by `qty`.
2. **TODO 2:** `lineTotal(line)`: `PRICES[line.code]` times `qty`, plus `extrasFor(line)`.
3. **TODO 3:** `cityOf(customer)`: the address's city, or `"Pickup at the stall"` when there's no address.
4. **TODO 4:** `pinOf(customer)`: the address's PIN, or `"PIN missing"` when there's no address or no PIN.
5. **TODO 5:** `qtyOrOne(qty?)`: the quantity, or `1` when it was left out. `0` stays `0`.

`?.` and `??` keep TODOs 3 to 5 to one line each. Press **Submit** to run the checks.
