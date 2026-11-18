# equals, hashCode and toString

The lost-bike report lists the same bike twice, and the log prints `Bike@1b6d3586` instead of something a person can read. Teach `Bike` how to describe itself and how to tell when two bikes are the same bike.

1. **TODO 1:** override `toString()` so it returns text like `Bike[B-1042, ebike]`.
2. **TODO 2:** override `equals(Object other)`. Two bikes are equal when their ids are equal. Anything that is not a `Bike` is never equal.
3. **TODO 3:** override `hashCode()` so it uses the id only, for example `Objects.hash(id)`. The file already has `import java.util.Objects;` at the top, so `Objects` is ready to use.
4. **TODO 4:** in `LostReport`, make `countDistinct(bikes)` count how many different bike ids the array holds. Use nested loops and `equals`.
5. **TODO 5:** make `contains(bikes, target)` return `true` when a bike in the array equals `target`.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
