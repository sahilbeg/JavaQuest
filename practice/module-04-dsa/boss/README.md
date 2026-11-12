# INC-04: The route planner sends riders to full stations

1. **Bronze:** `nearestFree(from)` never returns a station with 0 free docks.
2. **Silver:** a station missing from the free-docks map counts as 0 free docks, and the planner does not crash.
3. **Gold:** among equally near free stations, return the one with the most free docks, then the name that comes first alphabetically.
