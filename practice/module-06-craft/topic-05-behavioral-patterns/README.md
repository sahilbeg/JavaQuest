# Behavioral patterns: strategy, observer, command

Give Spoke swappable prices, ride-ended notifications and an undo button for admins. Money is in `long` cents.

`Station`, `PricingStrategy` and `AdminCommand` are given. Fill in the rest.

1. **TODO 1:** `StandardPricing.priceCents` returns 100 cents to unlock plus 15 cents a minute.
2. **TODO 2:** `RainDiscount` returns half of the price from the strategy it wraps, rounded down to whole cents.
3. **TODO 3:** `Surge` multiplies the price from the strategy it wraps by its multiplier, rounded down.
4. **TODO 4 to 6:** `RideEvents` keeps a list of subscribers. `subscribe` adds one and returns a `Runnable` that removes it. `rideEnded` calls every current subscriber in the order they subscribed.
5. **TODO 7 and 8:** `SetDocks.execute` remembers the old docks value and sets the new one. `undo` puts the old value back.
6. **TODO 9 to 12:** `CommandHistory.run` executes a command and remembers it. `undo` reverses the newest one and returns `true`, or returns `false` when there is nothing to undo. `size` says how many commands can still be undone.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
