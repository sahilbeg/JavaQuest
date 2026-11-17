# INC-07: The release pipeline is red

Kiosk 1.10.0 cannot ship, because three stages of the nightly pipeline are red. Every bug works fine on Tsunade's laptop.

1. **Bronze:** `Version.compareTo` compares numbers, so 1.10.0 is newer than 1.9.3 and the release gate lets it through.
2. **Silver:** `KioskConfig.load(configDir)` reads `kiosk.properties` from the folder it is given. With no file it returns the defaults. Missing keys get their default, and spaces around values are ignored.
3. **Gold:** `NightFare.surchargeCents()` reads the time from the clock it was given, so the nightly build gets the same answer as the day.

Run `Pipeline` to see each stage turn from RED to GREEN.
