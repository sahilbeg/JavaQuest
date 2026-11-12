# Reading and writing files (java.io)

Teach the kiosk to read and write its own ride log. The log is a CSV file: one ride per line, like `B-1042,Riverside,ebike,25`.

`Ride.java` is already done. Fill in `RideFiles.java`.

1. **TODO 1:** in `read`, open a `BufferedReader` around `new FileReader(file, StandardCharsets.UTF_8)` inside `try ( ... )`.
2. **TODO 2:** call `readLine()` in a loop until it returns `null`. Skip lines that are blank. A line with only spaces counts as blank: test `line.trim().isEmpty()`.
3. **TODO 3:** split each line on `","` and add a new `Ride`. Use `Integer.parseInt` for the minutes.
4. **TODO 4:** in `write`, open a `BufferedWriter` around `new FileWriter(file, StandardCharsets.UTF_8)` inside `try ( ... )`.
5. **TODO 5:** for each ride, write `bikeId,station,type,minutes` and then call `newLine()`.
6. **TODO 6:** in `countLines`, read the file line by line and count every line, blank ones too.

The tests make their own files in a temporary folder, so your real files stay safe.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
