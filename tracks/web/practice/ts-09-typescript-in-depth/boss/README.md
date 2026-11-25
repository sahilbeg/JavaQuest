# INC-09: An order status nobody handled

Fix `src/orderStatus.ts`. Since `RETURNED` was added to `OrderStatus`, returned orders show "Cancelled: undefined", and the Cancel button shows on orders that are already finished. `rupees` and `assertNever` are done for you.

1. **Bronze:** `statusLabel(status)` gives the right text for every kind:
   - PLACED: `Order placed`
   - PACKED: `Packed by Raju` (the real `packedBy`)
   - SHIPPED: `On the way with Swift Couriers` (the real `courier`)
   - DELIVERED: `Delivered on 14 Nov` (the real `deliveredOn`)
   - CANCELLED: `Cancelled: changed my mind` (the real `reason`)
   - RETURNED: `Returned, refund of ₹450.00 on its way` (the real `refundPaise`, through `rupees`)
2. **Silver:** `canCancel(status)` is `true` only for `PLACED` and `PACKED`, and `false` for every other kind.
3. **Gold:** a kind the code doesn't know, like `{ kind: "LOST" }` from an old server, makes both functions throw `Unknown order status: LOST`. Today the tracker calls it a cancellation instead.

`src/` is type-checked in strict mode before the checks run.
