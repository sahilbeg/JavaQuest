import { describe, it, expect } from "vitest";
import { isItemId, mapValues, validateOrder, hasErrors } from "../src/orderForm.ts";
import type { OrderForm } from "../src/orderForm.ts";

function goodForm(): OrderForm {
  return { name: "Asha Patil", phone: "98765 43210", pincode: "411004", qty: 2 };
}

describe("isItemId", () => {
  it("says true for item ids", () => {
    expect(isItemId("DC-1042"), "DC-1042 is an item id").toBe(true);
    expect(isItemId("DC-7"), "DC-7 is an item id too: DC- and at least one digit").toBe(true);
  });

  it("says false for anything else", () => {
    expect(isItemId("dc-1042"), "the letters must be capital DC").toBe(false);
    expect(isItemId("DC-"), "DC- needs digits after it").toBe(false);
    expect(isItemId("DC-10a2"), "only digits may follow DC-").toBe(false);
    expect(isItemId("AMI"), "an item code is not an item id").toBe(false);
    expect(isItemId("my DC-1042"), "nothing may come before DC-").toBe(false);
  });
});

describe("mapValues", () => {
  it("changes every value and keeps the keys", () => {
    const rupees = mapValues({ CST: 15000, KEY: 12000, AMI: 45000 }, (paise) => paise / 100);
    expect(rupees, "every price in paise should become rupees").toEqual({ CST: 150, KEY: 120, AMI: 450 });
  });

  it("passes each key to the function", () => {
    const labels = mapValues({ name: "Asha", pincode: "411004" }, (value, key) => `${String(key)}: ${value}`);
    expect(labels, "fn gets the value and its key").toEqual({ name: "name: Asha", pincode: "pincode: 411004" });
  });

  it("can mark every field of the form as untouched", () => {
    expect(mapValues(goodForm(), () => false), "every field of the form should be false").toEqual({ name: false, phone: false, pincode: false, qty: false });
  });

  it("doesn't change the object it was given", () => {
    const prices = { CST: 15000 };
    mapValues(prices, (paise) => paise * 2);
    expect(prices, "the original prices must stay the same").toEqual({ CST: 15000 });
  });
});

describe("validateOrder", () => {
  it("finds nothing wrong with a good form", () => {
    expect(validateOrder(goodForm()), "a good form should give {}").toEqual({});
  });

  it("asks for a name", () => {
    expect(validateOrder({ ...goodForm(), name: "   " }), "a name of only spaces is missing").toEqual({ name: "Please enter your name" });
  });

  it("checks the phone number has 10 digits", () => {
    expect(validateOrder({ ...goodForm(), phone: "98765 4321" }), "9 digits is too few").toEqual({ phone: "Phone needs 10 digits" });
    expect(validateOrder({ ...goodForm(), phone: "98765-43210" }), "a dash is not a digit").toEqual({ phone: "Phone needs 10 digits" });
    expect(validateOrder({ ...goodForm(), phone: "9876543210" }), "10 digits without a space is fine").toEqual({});
  });

  it("checks the PIN code has 6 digits", () => {
    expect(validateOrder({ ...goodForm(), pincode: "4110" }), "a 4-digit PIN code is wrong").toEqual({ pincode: "PIN code needs 6 digits" });
    expect(validateOrder({ ...goodForm(), pincode: "" }), "an empty PIN code is wrong").toEqual({ pincode: "PIN code needs 6 digits" });
  });

  it("checks the quantity is a whole number from 1 to 10", () => {
    expect(validateOrder({ ...goodForm(), qty: 0 }), "0 is too few").toEqual({ qty: "Quantity must be 1 to 10" });
    expect(validateOrder({ ...goodForm(), qty: 11 }), "11 is too many").toEqual({ qty: "Quantity must be 1 to 10" });
    expect(validateOrder({ ...goodForm(), qty: 2.5 }), "half an item can't be ordered").toEqual({ qty: "Quantity must be 1 to 10" });
    expect(validateOrder({ ...goodForm(), qty: 10 }), "10 is allowed").toEqual({});
  });

  it("reports every bad field at once", () => {
    const errors = validateOrder({ name: "", phone: "123", pincode: "41", qty: 0 });
    expect(Object.keys(errors).sort(), "all four fields are wrong").toEqual(["name", "phone", "pincode", "qty"]);
  });
});

describe("hasErrors", () => {
  it("says false for no errors", () => {
    expect(hasErrors({}), "{} has no messages").toBe(false);
  });

  it("says true when there is a message", () => {
    expect(hasErrors({ pincode: "PIN code needs 6 digits" }), "one message means the form has errors").toBe(true);
  });
});
