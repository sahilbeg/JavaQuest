# Exceptions: try, catch, finally

One bad line in the ride log crashes the whole import. Make `LogImporter.java` skip bad lines and count them.

1. **TODO 1 and 2:** make `parseMinutes(text)` return the number in `text`. When `Integer.parseInt` throws a `NumberFormatException`, catch it and return `-1`.
2. **TODO 3:** in `parseLine(line)`, split the line on `","`. If there are not exactly 4 parts, throw an `IllegalArgumentException` whose message includes the whole line.
3. **TODO 4:** use `parseMinutes` for the 4th part. If it gives `-1`, throw an `IllegalArgumentException` whose message includes the bad minutes text, such as `twenty`.
4. **TODO 5:** return a new `Ride` built from the bike id, station, type and minutes.
5. **TODO 6 and 7:** in `importAll(lines)`, try to parse each line and add it to `rides`. Catch `IllegalArgumentException`, add 1 to `badLines`, and move on to the next line.

This parseMinutes is different from the lesson's: it catches the problem itself and returns -1.

`Ride` and `ImportResult` are already written for you.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
