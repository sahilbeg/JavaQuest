## What happened

Dhaaga started taking returns on Monday. Kavya added a new member to the `OrderStatus` union, `RETURNED`, with the refund amount. The types compiled, the tests that existed passed, and it went live.

Then three things went wrong:

1. Every returned order showed "Cancelled: undefined" on the tracker. Customers thought their refunds were lost.
2. The Cancel button appeared on orders that were already returned or cancelled. Two customers pressed it.
3. An old server still sends a status called `LOST` for one parcel, and the tracker quietly called it "Cancelled" too.

Here is how a returned order reached the wrong line:

```animate
title: RETURNED falls through to the last line
kind: scene
nodes:
  - { id: server, label: Orders server, emoji: "🗄️" }
  - { id: label, label: statusLabel, emoji: "🏷️", value: idle }
  - { id: page, label: Order tracker, emoji: "📱" }
steps:
  - say: The server sends order-17's status. Its kind is RETURNED, with a refund of 45000 paise.
    send: { from: server, to: label, label: "{ kind: RETURNED, refundPaise: 45000 }" }
    set: { label: checking }
  - say: statusLabel checks PLACED, PACKED, SHIPPED and DELIVERED. None of them match.
    set: { label: "no match" }
  - say: The last line assumes that anything left must be a cancellation, so it reads status.reason. A returned order has no reason.
    set: { label: "reason is undefined" }
  - say: "The tracker shows \"Cancelled: undefined\". TypeScript never complained, because an as on that line told it not to check."
    send: { from: label, to: page, label: "Cancelled: undefined" }
  - say: The fix is a switch with a case for every kind, and a default that only a status nobody handled can reach.
    focus: [label]
```

## What you need

Everything here comes from this project.

### Syntax: one case per kind

```ts
function statusLabel(status: OrderStatus): string {
  switch (status.kind) {
    case "PLACED":
      return "Order placed";
    // ...one case for every other kind...
    default:
      return assertNever(status);
  }
}
```

Each `case` narrows `status` to one member of the union, so `status.refundPaise` is only allowed in the `RETURNED` case. Never write a last line that "must be" some member: that's exactly how `RETURNED` ended up as a cancellation.

### Syntax: assertNever

```ts
export function assertNever(status: never): never {
  throw new Error(`Unknown order status: ${(status as { kind: string }).kind}`);
}
```

It's already in `src/orderStatus.ts`. When every kind has a case, `status` in `default` has the type `never`, so the call compiles. Forget a kind, and TypeScript shows an error on that line before anything ships. If an unknown kind still arrives from a server, it throws a clear error instead of showing a wrong label.

### Which orders can be cancelled

An order can be cancelled only before it leaves the shop: when it is `PLACED` or `PACKED`. Once it's `SHIPPED` or `DELIVERED`, it's too late. A `CANCELLED` or `RETURNED` order is already finished.

### What the statuses look like

```ts
type OrderStatus =
  | { kind: "PLACED" }
  | { kind: "PACKED"; packedBy: string }
  | { kind: "SHIPPED"; courier: string }
  | { kind: "DELIVERED"; deliveredOn: string }
  | { kind: "CANCELLED"; reason: string }
  | { kind: "RETURNED"; refundPaise: number };
```

`rupees(45000)` gives `₹450.00`. It's done for you.

## The tiers

The fix goes in `src/orderStatus.ts`.

1. **Bronze** resolves the incident: `statusLabel` gives the right text for every kind.
   - PLACED: `Order placed`
   - PACKED: `Packed by Raju`
   - SHIPPED: `On the way with Swift Couriers`
   - DELIVERED: `Delivered on 14 Nov`
   - CANCELLED: `Cancelled: changed my mind`
   - RETURNED: `Returned, refund of ₹450.00 on its way`
2. **Silver**: `canCancel` is `true` only for `PLACED` and `PACKED`, and `false` for every other kind.
3. **Gold**: a kind the code doesn't know, like `{ kind: "LOST" }`, makes both `statusLabel` and `canCancel` throw `Unknown order status: LOST`.

## Recap

- A union with a tag is only safe when every member is handled. A "must be the last one" line hides new members.
- A `switch` on the tag narrows each case, so each member's own fields are used only where they exist.
- `default: return assertNever(status)` turns a forgotten member into a type error, and an unknown one at run time into a clear error.
- Say yes for the cases you mean (`PLACED`, `PACKED`), not "everything except" a list, so new members start out as no.

## Common mistakes

**Adding a RETURNED case and keeping the fall-through line.** Bronze passes, but the next new kind will fall through again, and `LOST` is still called a cancellation. Make the last branch `assertNever`.

**Fixing canCancel with another "!==".** Adding `&& status.kind !== "CANCELLED"` fixes today, but every new kind will be cancellable by default. List the kinds that can be cancelled instead.

**Using default to return false in canCancel.** It compiles, but it hides forgotten kinds, and `LOST` returns `false` instead of throwing. Give every kind a case and keep `default` for `assertNever`.
