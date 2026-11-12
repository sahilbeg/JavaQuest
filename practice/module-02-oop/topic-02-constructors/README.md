# Constructors and this

Make it impossible to build a bike or a station without the basics. Both classes already declare their constructors, but the bodies are empty.

In `Bike.java`:

1. **TODO 1:** make `Bike(String id, String type)` copy both parameters into the fields with `this`.
2. **TODO 2:** make `Bike(String id)` call the other constructor with `this(...)`, using the type `"classic"`.

In `Station.java`:

3. **TODO 3:** make `Station(String name)` set the name and give the station 12 docks.
4. **TODO 4:** make `Station(String name, int docks)` set both fields.
5. **TODO 5:** make `freeDocks(int bikesDocked)` return how many docks are still empty.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
