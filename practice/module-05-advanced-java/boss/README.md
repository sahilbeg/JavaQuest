# INC-05: Double unlock at rush hour

1. **Bronze:** when 50 riders unlock the same bike at once, exactly one succeeds.
2. **Silver:** 8 threads x 10,000 `recordPayment()` calls give exactly 80,000.
3. **Gold:** moving bikes both ways between two stations never freezes. A frozen test fails after 5 seconds with a timeout.
