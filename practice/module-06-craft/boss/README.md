# INC-06: Invoices changed after the billing refactor

`LegacyBilling` is the reference. Do not change it. Change `Billing` until it behaves exactly the same.

1. **Bronze:** only BASIC riders pay the unlock fee.
2. **Silver:** every fare matches `LegacyBilling` to the cent, for every type, membership and 0 to 90 minutes.
3. **Gold:** `invoice` gives the legacy text and leaves the caller's list in its original order.
