# Abstract classes

Kakashi found a plain `Vehicle` in the kiosk with no price. Its rides cost only the unlock fee. Make `Vehicle` a safe base class and give every real bike its own price.

`Vehicle` is already `abstract`. It declares two abstract methods: `pricePerMinute()` and `kind()`.

1. **TODO 1:** in `Vehicle`, make `fare(int minutes)` return `1.00 + minutes * pricePerMinute()`.
2. **TODO 2:** in `Vehicle`, make `label()` return `kind() + " " + id`, for example `ebike B-1042`.
3. **TODO 3 and 4:** in `ClassicBike`, return `0.15` and `"classic"`.
4. **TODO 5 and 6:** in `EBike`, return `0.30` and `"ebike"`.
5. **TODO 7 and 8:** in `CargoBike`, return `0.25` and `"cargo"`.

Keep the word `abstract` on `Vehicle`. One test checks it.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
