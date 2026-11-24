import { describe, it, expect } from "vitest";
import { createBasket, copyOrder } from "../src/orders.js";

function order17() {
  return {
    id: "order-17",
    customer: { name: "Mr. Fernandes", phone: "98765 43210" },
    lines: [
      { code: "AMI", qty: 2 },
      { code: "KEY", qty: 1 },
    ],
  };
}

describe("createBasket", () => {
  it("adds a line to the basket it was called on", () => {
    const basket = createBasket("Asha");
    basket.add("CST", 15000, 2);
    expect(basket.lines.length, "one add should make one line").toBe(1);
    expect(basket.lines[0], "the line should hold the code, the paise and the quantity").toEqual({ code: "CST", paise: 15000, qty: 2 });
  });

  it("uses a quantity of 1 when none is given", () => {
    const basket = createBasket("Asha");
    basket.add("KEY", 12000);
    expect(basket.lines[0] && basket.lines[0].qty, "add without a quantity should add 1").toBe(1);
  });

  it("returns the basket so adds can be chained", () => {
    const basket = createBasket("Asha");
    const result = basket.add("CST", 15000, 2);
    expect(result, "add should return the basket itself").toBe(basket);
    basket.add("KEY", 12000).add("AMI", 45000);
    expect(basket.lines.length, "three adds should make three lines").toBe(3);
  });

  it("works out the total from the basket's own lines", () => {
    const basket = createBasket("Asha");
    basket.add("CST", 15000, 2).add("KEY", 12000);
    expect(basket.total(), "two coaster sets and a keychain cost 42000 paise").toBe(42000);
  });

  it("gives 0 for an empty basket", () => {
    expect(createBasket("Ravi").total(), "an empty basket's total is 0").toBe(0);
  });

  it("keeps two baskets apart", () => {
    const asha = createBasket("Asha");
    const ravi = createBasket("Ravi");
    asha.add("BAG", 89900);
    ravi.add("AMI", 45000, 2);
    expect(asha.total(), "Asha's basket should only hold her bag").toBe(89900);
    expect(ravi.total(), "Ravi's basket should only hold his two elephants").toBe(90000);
  });
});

describe("copyOrder", () => {
  it("returns an order with the same contents", () => {
    expect(copyOrder(order17()), "the copy should look exactly like order-17").toEqual(order17());
  });

  it("returns a new object", () => {
    const original = order17();
    expect(copyOrder(original) === original, "the copy must be a new object, not the same one").toBe(false);
  });

  it("keeps the original's lines when a copied line changes", () => {
    const original = order17();
    const copy = copyOrder(original);
    copy.lines[0].qty = 3;
    expect(original.lines[0].qty, "order-17 must still have 2 elephants after the copy changed to 3").toBe(2);
  });

  it("keeps the original's lines when a line is added to the copy", () => {
    const original = order17();
    const copy = copyOrder(original);
    copy.lines.push({ code: "BTY", qty: 1 });
    expect(original.lines.length, "order-17 must still have 2 lines").toBe(2);
  });

  it("keeps the original's customer when the copy's customer changes", () => {
    const original = order17();
    const copy = copyOrder(original);
    copy.customer.name = "Ms. Fernandes";
    expect(original.customer.name, "order-17 must still belong to Mr. Fernandes").toBe("Mr. Fernandes");
  });
});
