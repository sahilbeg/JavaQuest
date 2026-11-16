# FEAT-07: Package the kiosk as a runnable jar

Fill in the numbered TODOs in package `jq.m07.feature`:

1. TODOs 1 to 3: `Kiosk.run` handles `12`, `--version` and wrong arguments, and returns the exit code.
2. TODO 4: `BuildInfo.version()` reads the version from the jar's manifest, or says `dev`.
3. TODOs 5 to 10: `JarPackager.pack` writes a jar with a manifest first, every file sorted by name, and one fixed local date and time on every entry.

The last checks pack your own compiled kiosk into a jar and start it with `java -jar`, exactly as a kiosk computer would.
