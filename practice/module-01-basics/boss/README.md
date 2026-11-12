# INC-01: The kiosk charges the wrong fare after midnight

Times are minutes since midnight: 23:50 is 1430 and 00:20 is 20.

1. **Bronze:** `minutesBetween(start, end)` works across midnight. 1430 to 20 is 30 minutes.
2. **Silver:** `fare(start, end, type, member)` follows the rules. Non-members pay 1.00 plus the price per minute. Members ride 30 minutes free, then pay per minute, with no unlock fee. Unknown types cost -1.
3. **Gold:** `receipt(start, end, type, member)` returns a line like `23:50-00:20 30 min $5.50`.
