# NIO: Path and Files

Rewrite the kiosk's log code with `Path` and `Files`, and archive yesterday's log.

Fill in `LogArchive.java`.

1. **TODO 1:** in `appendRide`, add `csvLine` plus `"\n"` to the end of the log with `Files.writeString`. Pass `StandardCharsets.UTF_8`, `StandardOpenOption.CREATE` and `StandardOpenOption.APPEND`, so a missing file gets created.
2. **TODO 2:** in `lines`, return an empty list when the log does not exist. Otherwise return `Files.readAllLines(log, StandardCharsets.UTF_8)`.
3. **TODO 3:** in `archive`, create the archive folder with `Files.createDirectories`.
4. **TODO 4:** build the target path `archiveDir/rides-<dateLabel>.csv` with `resolve`. Move the log there with `Files.move` and return the new path.
5. **TODO 5:** in `listArchives`, loop over `Files.newDirectoryStream(archiveDir)` and add each `getFileName().toString()` to the list.
6. **TODO 6:** sort the names with `Collections.sort` before you return them.

The tests make their own files in a temporary folder, so your real files stay safe.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
