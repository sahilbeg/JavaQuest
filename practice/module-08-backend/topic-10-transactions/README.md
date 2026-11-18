# Transactions

Make ride-credit gifts all or nothing, so no rider ever loses credit that their friend never gets.

`GiftService` already does the work. `gift` withdraws from the sender, deposits to the friend, then writes a `GiftLog` row. `giftToEach` calls `gift` once per friend. The code is right, but each step commits on its own. A failure halfway leaves the money half moved.

1. **TODO 1:** make `gift` one transaction. It must also roll back on `InsufficientCreditException`, which is a checked exception.
2. **TODO 2:** `giftToEach` calls `gift` through `this`, which skips Spring's proxy, so `gift`'s transaction never starts there. Give `giftToEach` its own transaction with the same rollback rule.

You only add annotations. Do not change the steps inside the methods.

## How to read the tests

Open `GiftServiceTest.java`. It uses `@SpringBootTest` with no web server. Before each test, it gives four riders fresh wallets: Ada (`r-17`) 2000 cents, Lin (`r-21`) 100, Omar (`r-33`) 0 and Zoe (`r-40`) 50. Rider `r-99` has no wallet.

- The test class has no `@Transactional` on purpose. Every balance check reads the database in a fresh transaction, so it sees only what your code really committed.
- The good-gift test already passes with the starting code. Things only break when a step fails in the middle.
- A gift to `r-99` fails after the withdrawal. Ada must still have 2000 cents.
- `giftToEach` must undo every earlier gift when a later one fails, both for an unknown friend and for `InsufficientCreditException`.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own. A Spring test takes a few seconds to start.
