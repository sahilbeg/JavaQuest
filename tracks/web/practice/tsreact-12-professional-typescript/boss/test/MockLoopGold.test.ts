import { describe, it, expect } from "vitest";
import { createEventBus } from "../src/eventBus.ts";

interface StoreEvents {
  added: { id: string; qty: number };
  paid: { orderId: string; paise: number };
}

describe("Round 3: createEventBus", () => {
  it("calls every handler for an event, with its payload", () => {
    const bus = createEventBus<StoreEvents>();
    const seen: string[] = [];
    bus.on("added", (item) => seen.push("badge " + item.id));
    bus.on("added", (item) => seen.push("toast " + item.qty));
    bus.emit("added", { id: "DC-1042", qty: 2 });
    expect(seen, "both handlers for \"added\" should run, in the order they subscribed").toEqual(["badge DC-1042", "toast 2"]);
  });

  it("doesn't call handlers for other events", () => {
    const bus = createEventBus<StoreEvents>();
    const seen: string[] = [];
    bus.on("paid", (payment) => seen.push(payment.orderId));
    bus.emit("added", { id: "DC-1042", qty: 1 });
    expect(seen, "an \"added\" event must not reach a \"paid\" handler").toEqual([]);
  });

  it("unsubscribes only the subscription it came from", () => {
    const bus = createEventBus<StoreEvents>();
    const seen: string[] = [];
    const offBadge = bus.on("added", () => seen.push("badge"));
    bus.on("added", () => seen.push("toast"));
    offBadge();
    bus.emit("added", { id: "DC-1042", qty: 1 });
    expect(seen, "after offBadge(), only the toast handler should run").toEqual(["toast"]);
  });

  it("is harmless to unsubscribe twice", () => {
    const bus = createEventBus<StoreEvents>();
    const seen: string[] = [];
    const offBadge = bus.on("added", () => seen.push("badge"));
    bus.on("added", () => seen.push("toast"));
    offBadge();
    offBadge();
    bus.emit("added", { id: "DC-1042", qty: 1 });
    expect(seen, "calling offBadge() a second time must not remove the toast handler: indexOf gives -1, and splice(-1, 1) removes the last one (TODO 4)").toEqual(["toast"]);
  });

  it("lets the same function subscribe twice and leave once", () => {
    const bus = createEventBus<StoreEvents>();
    const seen: string[] = [];
    const count = () => seen.push("count");
    const offFirst = bus.on("added", count);
    bus.on("added", count);
    offFirst();
    bus.emit("added", { id: "DC-1042", qty: 1 });
    expect(seen, "two subscriptions minus one should leave one call").toEqual(["count"]);
  });

  it("still reaches the next handler when a handler unsubscribes itself during emit", () => {
    const bus = createEventBus<StoreEvents>();
    const seen: string[] = [];
    const offFirst = bus.on("added", () => {
      offFirst();
      seen.push("first");
    });
    bus.on("added", () => seen.push("second"));
    bus.emit("added", { id: "DC-1042", qty: 1 });
    expect(seen, "the second handler must still get this event: emit should loop over a list that can't change under it (TODO 4)").toEqual(["first", "second"]);
    bus.emit("added", { id: "DC-1042", qty: 1 });
    expect(seen, "on the next event, only the second handler is left").toEqual(["first", "second", "second"]);
  });

  it("runs a once handler for the next event only", () => {
    const bus = createEventBus<StoreEvents>();
    const paid: string[] = [];
    bus.once("paid", (payment) => paid.push(payment.orderId));
    bus.emit("paid", { orderId: "order-17", paise: 51000 });
    bus.emit("paid", { orderId: "order-18", paise: 12000 });
    expect(paid, "a once handler should see order-17 and nothing after it (TODO 5)").toEqual(["order-17"]);
  });

  it("can cancel a once handler before it fires", () => {
    const bus = createEventBus<StoreEvents>();
    const paid: string[] = [];
    const cancel = bus.once("paid", (payment) => paid.push(payment.orderId));
    cancel();
    bus.emit("paid", { orderId: "order-17", paise: 51000 });
    expect(paid, "a cancelled once handler should never run").toEqual([]);
  });
});
