import { describe, it, expect, afterEach } from "vitest";
import { validateOrder } from "../src/order-form.js";

const cleanups = [];

afterEach(() => {
  while (cleanups.length > 0) {
    cleanups.pop()();
  }
  document.body.innerHTML = "";
});

const NAME = "Please enter your name.";
const PHONE = "Please enter a 10-digit phone number.";
const QTY = "Choose between 1 and 20 pieces.";

function openForm() {
  const element = document.createElement("custom-order-form");
  document.body.append(element);
  return element;
}

const field = (element, name) => element.querySelector(`[name="${name}"]`);
const errorFor = (element, name) => document.getElementById(field(element, name)?.getAttribute("aria-describedby") ?? "");

function fill(element, values) {
  for (const [name, value] of Object.entries(values)) {
    field(element, name).value = value;
  }
}

/** Submits the form the way pressing Send would. Returns false when the page reload was stopped. */
function submit(element) {
  return element.querySelector("form").dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
}

/** Listens on document for order-request until the test ends, and returns the details heard. */
function hearRequests() {
  const heard = [];
  const listener = (event) => heard.push(event.detail);
  document.addEventListener("order-request", listener);
  cleanups.push(() => document.removeEventListener("order-request", listener));
  return heard;
}

describe("validateOrder", () => {
  it("finds nothing wrong with a good order", () => {
    expect(validateOrder({ name: "Mrs. Kulkarni", phone: "98765 43210", qty: "2" }), "a good order should give {}").toEqual({});
  });

  it("asks for a name when it is empty or only spaces", () => {
    expect(validateOrder({ name: "", phone: "9876543210", qty: "1" }).name, "an empty name should give the name message").toBe(NAME);
    expect(validateOrder({ name: "   ", phone: "9876543210", qty: "1" }).name, "a name of only spaces should give the name message").toBe(NAME);
  });

  it("wants 10 digits for the phone, spaces allowed", () => {
    expect(validateOrder({ name: "Asha", phone: "98765", qty: "1" }).phone, "5 digits should give the phone message").toBe(PHONE);
    expect(validateOrder({ name: "Asha", phone: "98765 4321a", qty: "1" }).phone, "a letter in the phone should give the phone message").toBe(PHONE);
    expect(validateOrder({ name: "Asha", phone: "98765 43210", qty: "1" }).phone, "98765 43210 is 10 digits once the space is removed").toBeUndefined();
  });

  it("wants a whole number of pieces from 1 to 20", () => {
    for (const qty of ["0", "21", "2.5", ""]) {
      expect(validateOrder({ name: "Asha", phone: "9876543210", qty }).qty, `a quantity of "${qty}" should give the quantity message`).toBe(QTY);
    }
    expect(validateOrder({ name: "Asha", phone: "9876543210", qty: "20" }).qty, "20 pieces should be fine").toBeUndefined();
  });
});

describe("<custom-order-form>", () => {
  it("stops the browser from sending the form and reloading", () => {
    const element = openForm();
    fill(element, { name: "Asha", phone: "9876543210", qty: "1" });
    expect(submit(element), "the submit event should be cancelled with preventDefault").toBe(false);
  });

  it("shows an error next to each wrong field", () => {
    const element = openForm();
    fill(element, { name: "", phone: "123", qty: "1" });
    submit(element);
    expect(errorFor(element, "name")?.textContent, "the name's error paragraph should say the name message").toBe(NAME);
    expect(field(element, "name").getAttribute("aria-invalid"), "the name field should have aria-invalid=\"true\"").toBe("true");
    expect(errorFor(element, "phone")?.textContent, "the phone's error paragraph should say the phone message").toBe(PHONE);
    expect(field(element, "phone").getAttribute("aria-invalid"), "the phone field should have aria-invalid=\"true\"").toBe("true");
    expect(errorFor(element, "qty")?.textContent, "a good quantity should have an empty error paragraph").toBe("");
    expect(field(element, "qty").hasAttribute("aria-invalid"), "a good quantity should have no aria-invalid").toBe(false);
  });

  it("clears an error once the field is fixed", () => {
    const element = openForm();
    fill(element, { name: "Asha", phone: "123", qty: "1" });
    submit(element);
    fill(element, { phone: "98765 43210", qty: "50" });
    submit(element);
    expect(errorFor(element, "phone")?.textContent, "after the phone is fixed, its error should be empty").toBe("");
    expect(field(element, "phone").hasAttribute("aria-invalid"), "after the phone is fixed, aria-invalid should be removed").toBe(false);
    expect(errorFor(element, "qty")?.textContent, "the quantity of 50 should now show its message").toBe(QTY);
  });

  it("moves focus to the first wrong field", () => {
    const element = openForm();
    fill(element, { name: "Asha", phone: "123", qty: "0" });
    submit(element);
    expect(document.activeElement, "with the phone and quantity wrong, focus should go to the phone field").toBe(field(element, "phone"));
  });

  it("sends nothing while something is wrong", () => {
    const heard = hearRequests();
    const element = openForm();
    fill(element, { name: "Asha", phone: "123", qty: "1" });
    submit(element);
    expect(heard.length, "a form with a wrong phone should not send order-request").toBe(0);
  });

  it("sends order-request with clean values when everything is right", () => {
    const heard = hearRequests();
    const element = openForm();
    fill(element, { name: "  Mrs. Kulkarni ", phone: "98765 43210", qty: "3" });
    submit(element);
    expect(heard, "the page should hear one order-request with the trimmed name, the phone's digits and the number 3").toEqual([
      { name: "Mrs. Kulkarni", phone: "9876543210", qty: 3 },
    ]);
  });

  it("empties the form after sending", () => {
    hearRequests();
    const element = openForm();
    fill(element, { name: "Asha", phone: "9876543210", qty: "4" });
    submit(element);
    expect(field(element, "name").value, "after sending, the name should be empty again").toBe("");
    expect(field(element, "qty").value, "after sending, the quantity should be back to 1").toBe("1");
  });

  it("keeps what was typed when it is moved", () => {
    const element = openForm();
    fill(element, { name: "Asha" });
    element.remove();
    document.body.append(element);
    expect(element.querySelectorAll("form").length, "the element should still hold exactly one form").toBe(1);
    expect(field(element, "name").value, "moving the form should keep the typed name").toBe("Asha");
  });
});
