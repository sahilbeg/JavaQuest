# Testing typed components

The account page's `OrderCard` has a new status, `"packed"`, and its tests never tried it. Build the typed test helpers the team will use from now on, in `src/fixtures.ts`. The checks use your helpers to test `OrderCard` in every status.

`OrderCard.tsx`, `types.ts` and `money.ts` already work. Don't change them.

1. **TODO 1:** `makeOrder(overrides)` returns a complete `Order`: `order-17`, customer `Meera Kulkarni`, status `"placed"`, and one Golu line (a copy of `GOLU_LINE`). The fields in `overrides` replace the defaults. Every call must build a **new** lines array with a **new** line object, so one test can't change another test's order.
2. **TODO 2:** fill in `STATUS_CASES`, one row per status, from Sana's design:

   | Status | Label | Cancel order button | Track parcel button |
   |---|---|---|---|
   | `placed` | `Order placed` | yes | no |
   | `packed` | `Packed, waiting for the courier` | yes | no |
   | `shipped` | `On its way` | no | yes |
   | `delivered` | `Delivered` | no | no |
   | `cancelled` | `Cancelled` | no | no |

3. **TODO 3:** `makeSpy<Args>()` returns `{ fn, calls }`. Each call of `fn(...args)` adds its arguments, as one array, to the end of `calls`. Each spy has its own `calls`.

Everything must type-check in strict mode. Don't use `any` or `as`.
