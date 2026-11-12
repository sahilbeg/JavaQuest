# The collections framework map

The Riverside kiosk keeps its ride log in an array of 100 slots, so it crashes on ride 101. Swap the array for a list that grows.

Open `RideLog.java`. It holds a `List<String>` of bike ids, like `"B-1042"`.

1. **TODO 1:** make the `rides` field start as an empty `ArrayList`.
2. **TODO 2:** make `add(String bikeId)` put the bike id on the end of the list.
3. **TODO 3:** make `count()` return how many rides the log holds.
4. **TODO 4:** make `get(int index)` return the bike id at that position. Position 0 is the first ride.
5. **TODO 5:** make the static method `pickCollection(String job)` return the right collection name for each job:

| job | returns |
|---|---|
| `"keep rides in order"` | `"List"` |
| `"unique bike ids"` | `"Set"` |
| `"minutes per bike"` | `"Map"` |

For any other job text, return `""`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
