import { describe, it, expect } from "vitest";
import { Cart } from "../src/Cart.js";

const golu = { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 };
const coasters = { id: "DC-1043", code: "CST", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 12 };
const bag = { id: "DC-1045", code: "BAG", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 };

const ids = (cart) => cart.lines().map((line) => line.id).join(",");

describe("Cart", () => {
  it("remembers whose cart it is", () => {
    expect(new Cart("Asha").customer, "new Cart(\"Asha\") should have customer Asha").toBe("Asha");
  });

  it("starts empty", () => {
    const cart = new Cart("Asha");
    expect(cart.lines().length, "a new cart has no lines").toBe(0);
    expect(cart.count, "a new cart holds 0 items").toBe(0);
    expect(cart.total, "a new cart costs 0 paise").toBe(0);
  });

  it("adds an item as a line with its id, name, price and quantity", () => {
    const cart = new Cart("Asha");
    cart.add(golu, 2);
    expect(cart.lines(), "adding two Golus should make one line with qty 2").toEqual([
      { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 2 },
    ]);
  });

  it("adds 1 when no quantity is given", () => {
    const cart = new Cart("Asha");
    cart.add(bag);
    expect(cart.count, "add(bag) with no quantity should add one bag").toBe(1);
  });

  it("raises the quantity when the same item is added again", () => {
    const cart = new Cart("Asha");
    cart.add(golu);
    cart.add(coasters);
    cart.add(golu);
    expect(ids(cart), "Golu twice should still be one line, in its first place").toBe("DC-1042,DC-1043");
    const first = cart.lines()[0] || {};
    expect(first.qty, "Golu's line should now say 2").toBe(2);
  });

  it("returns the cart from add, so calls can be chained", () => {
    const cart = new Cart("Asha");
    expect(cart.add(golu), "add should return the cart itself").toBe(cart);
  });

  it("removes a line by id", () => {
    const cart = new Cart("Ravi");
    cart.add(golu);
    cart.add(bag);
    cart.remove("DC-1042");
    expect(ids(cart), "only the bag should be left after removing Golu").toBe("DC-1045");
  });

  it("ignores an id that is not in the cart", () => {
    const cart = new Cart("Ravi");
    cart.add(golu);
    cart.add(bag);
    cart.remove("DC-9999");
    expect(ids(cart), "removing a missing id should change nothing").toBe("DC-1042,DC-1045");
  });

  it("counts every item, not every line", () => {
    const cart = new Cart("Asha");
    cart.add(golu, 2);
    cart.add(coasters, 3);
    expect(cart.count, "2 Golus and 3 coaster sets make 5 items").toBe(5);
  });

  it("works out the total in paise", () => {
    const cart = new Cart("Asha");
    cart.add(golu, 2);
    cart.add(coasters, 3);
    cart.add(bag);
    expect(cart.total, "90000 + 45000 + 89900 is 224900 paise").toBe(224900);
  });

  it("hands out copies of the lines", () => {
    const cart = new Cart("Asha");
    cart.add(golu);
    const lines = cart.lines();
    if (lines.length > 0) {
      lines[0].qty = 50;
    }
    lines.push({ id: "DC-1046", name: "Rani pink baby booties", paise: 35000, qty: 1 });
    expect(cart.count, "changing the array from lines() must not change the cart").toBe(1);
  });

  it("keeps its lines private", () => {
    const cart = new Cart("Asha");
    cart.add(golu);
    expect(Object.keys(cart).join(","), "the only public field should be customer").toBe("customer");
  });
});
