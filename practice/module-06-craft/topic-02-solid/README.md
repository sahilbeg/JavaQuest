# SOLID principles

`PaymentProcessor` has a switch with one branch per payment method. Every new method means editing it, and one edit broke card payments. Reshape it so a new method is a new class, and `PaymentProcessor` never changes again. Money is in **long cents**.

1. **TODO 1:** make `CardPayment` implement `PaymentMethod`. `name()` returns `card`. `pay(250)` returns `card: charged 250 cents`.
2. **TODO 2:** do the same for `WalletPayment`, with the name `wallet`.
3. **TODO 3:** do the same for the new `TransitPassPayment`, with the name `transit`.
4. **TODO 4:** store the `List<PaymentMethod>` that the constructor receives.
5. **TODO 5:** in `pay`, remove the switch. Find the method whose `name()` matches and call its `pay`. If none matches, throw `IllegalArgumentException("unknown payment method: " + method)`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
