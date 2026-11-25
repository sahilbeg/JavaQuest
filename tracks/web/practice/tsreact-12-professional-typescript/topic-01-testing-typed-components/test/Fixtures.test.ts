import { describe, it, expect } from "vitest";
import { GOLU_LINE, makeOrder, makeSpy } from "../src/fixtures.ts";

const GOLU = { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 };
const TOTE = { id: "DC-1301", name: "Rani pink tote bag", paise: 89900, qty: 1 };

describe("makeOrder", () => {
  it("builds order-17 for Meera, placed, with one Golu, when given nothing", () => {
    expect(makeOrder(), "makeOrder() should give order-17 for Meera Kulkarni, status \"placed\", with one Golu line (TODO 1)").toEqual({
      id: "order-17",
      customer: "Meera Kulkarni",
      status: "placed",
      lines: [GOLU],
    });
  });

  it("changes only the fields a test passes in", () => {
    const order = makeOrder({ status: "shipped", customer: "Raju Pawar" });
    expect(order.status, "makeOrder({ status: \"shipped\" }) should give a shipped order: spread overrides last (TODO 1)").toBe("shipped");
    expect(order.customer, "makeOrder({ customer: \"Raju Pawar\" }) should use Raju as the customer").toBe("Raju Pawar");
    expect(order.id, "fields you don't pass keep their defaults, so the id stays \"order-17\"").toBe("order-17");
    expect(order.lines, "fields you don't pass keep their defaults, so there is still one Golu").toEqual([GOLU]);
  });

  it("gives every test its own lines, so one test can't spoil the next", () => {
    const first = makeOrder();
    first.lines.push({ ...TOTE });
    first.lines[0].qty = 5;
    const second = makeOrder();
    expect(second.lines, "changing one order's lines must not change the next order: build a new array, with a copy of GOLU_LINE, on every call").toEqual([GOLU]);
    expect(GOLU_LINE.qty, "GOLU_LINE itself must stay at qty 1: copy it with { ...GOLU_LINE } instead of putting it in the array").toBe(1);
  });
});

describe("makeSpy", () => {
  it("starts with no calls", () => {
    const spy = makeSpy<[orderId: string]>();
    expect(spy.calls, "a new spy has not been called yet").toEqual([]);
  });

  it("remembers the arguments of every call, in order", () => {
    const spy = makeSpy<[id: string, qty: number]>();
    spy.fn("DC-1042", 1);
    spy.fn("DC-1301", 2);
    expect(spy.calls, "two calls should give two entries, each holding that call's arguments (TODO 3)").toEqual([
      ["DC-1042", 1],
      ["DC-1301", 2],
    ]);
  });

  it("keeps each spy's calls to itself", () => {
    const cancel = makeSpy<[orderId: string]>();
    const track = makeSpy<[orderId: string]>();
    cancel.fn("order-17");
    expect(cancel.calls, "the spy that was called should remember the call (TODO 3)").toEqual([["order-17"]]);
    expect(track.calls, "a different spy must not see that call: make a new calls array inside makeSpy").toEqual([]);
  });
});
