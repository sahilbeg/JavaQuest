# Classes and objects: blueprints and bikes

Turn the loose bike variables into one `Bike` blueprint in `Bike.java`. Each bike already has three public fields: `id`, `type` and `minutesRidden`.

1. **TODO 1:** make `addRide(int minutes)` add the minutes to this bike's `minutesRidden`.
2. **TODO 2:** make `describe()` return text like `B-1042 (classic), 45 min ridden`, using this bike's own fields.
3. **TODO 3:** make `fare(int minutes)` return the price of a ride on this bike. The unlock fee is $1.00. Per minute, `"classic"` costs 0.15, `"ebike"` costs 0.30 and `"cargo"` costs 0.25.

Build bikes with `new Bike()` and set their fields with the dot operator. Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
