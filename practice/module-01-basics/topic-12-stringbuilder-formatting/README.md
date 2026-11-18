# StringBuilder and text formatting

Make the Spoke receipt tidy. Every price shows 2 decimals, and the prices line up in one column.

1. **TODO 1:** `money(amount)` returns the amount with a dollar sign and exactly 2 decimals. `money(2.4999999)` gives `"$2.50"`. Use `String.format("$%.2f", amount)`.
2. **TODO 2:** `line(label, amount)` returns `String.format("%-12s%8s", label, money(amount))`. The label is padded to 12 characters on the right. The money is right-aligned in 8 characters. So `line("Unlock fee", 1.0)` gives `"Unlock fee     $1.00"`, which is 20 characters long.
3. **TODO 3 to 5:** `receipt(labels, amounts)` uses a `StringBuilder`. For each item, append `line(labels[i], amounts[i])` and then `"\n"`, and add the amount to a total. Finish with `line("TOTAL", total)`, with no `"\n"` after it. Return `sb.toString()`.

For labels `{"Unlock fee", "Ride 10 min"}` and amounts `{1.0, 1.5}` the receipt is:

```
Unlock fee     $1.00
Ride 10 min    $1.50
TOTAL          $2.50
```

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
