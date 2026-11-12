# INC-02: E-bikes are billed as normal bikes

1. **Bronze:** an `EBike` fare uses 0.30 per minute, even through a `Bike` variable.
2. **Silver:** a `CargoBike` fare uses 0.25 per minute, even through a `Bike` variable.
3. **Gold:** `Billing.invoice(bikes, minutes)` returns this text:

```text
B-2001 ebike     10 min  $4.00
B-3001 cargo     20 min  $6.00
TOTAL                   $10.00
```

Each ride line is `String.format("%-7s%-9s%3d min %6s", id, type, minutes, money(fare))`. The total line is `String.format("%-23s%7s", "TOTAL", money(total))`. Join the lines with `\n`, with no newline at the end.
