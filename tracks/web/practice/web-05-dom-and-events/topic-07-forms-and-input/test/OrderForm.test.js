import { describe, it, expect, beforeEach, vi } from "vitest";
import { validate, setupStitchCounter, setupOrderForm } from "../src/order-form.js";

beforeEach(() => {
  document.body.innerHTML = `
    <form id="order" novalidate>
      <input id="name" name="name">
      <p class="error" data-error-for="name"></p>
      <input id="phone" name="phone">
      <p class="error" data-error-for="phone"></p>
      <input id="pin" name="pin">
      <p class="error" data-error-for="pin"></p>
      <input id="stitch" name="stitch">
      <span id="stitch-count"></span>
      <button type="submit">Place order</button>
    </form>`;
});

const GOOD = { name: "Anjali Deshpande", phone: "98765 43210", pin: "411007" };
const $ = (selector) => document.querySelector(selector);

function fill(values) {
  for (const [field, value] of Object.entries(values)) {
    $(`#${field}`).value = value;
  }
}

/** Sends the form like a browser would. Returns false when the handler called preventDefault. */
function submit() {
  return $("#order").dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
}

function type(text) {
  $("#stitch").value = text;
  $("#stitch").dispatchEvent(new Event("input", { bubbles: true }));
}

describe("validate", () => {
  it("finds nothing wrong with good details", () => {
    expect(validate(GOOD), "a name, a 10-digit phone (spaces allowed) and a 6-digit PIN have no errors").toEqual({});
  });

  it("asks for a name when it is only spaces", () => {
    expect(validate({ ...GOOD, name: "   " }).name, "a name of spaces should give \"Please enter your name.\"").toBe("Please enter your name.");
  });

  it("refuses a 9-digit phone number", () => {
    expect(validate({ ...GOOD, phone: "98765 4321" }).phone, "9 digits should give \"Enter a 10-digit phone number.\"").toBe("Enter a 10-digit phone number.");
  });

  it("refuses a phone number with a letter in it", () => {
    expect(validate({ ...GOOD, phone: "98765 4321O" }).phone, "a letter O is not a zero, so this phone should give an error").toBe("Enter a 10-digit phone number.");
  });

  it("refuses PIN codes that are too short or too long", () => {
    expect(validate({ ...GOOD, pin: "41100" }).pin, "5 digits should give \"Enter a 6-digit PIN code.\"").toBe("Enter a 6-digit PIN code.");
    expect(validate({ ...GOOD, pin: "4110077" }).pin, "7 digits should give \"Enter a 6-digit PIN code.\"").toBe("Enter a 6-digit PIN code.");
  });

  it("accepts a PIN code with spaces around it", () => {
    expect(validate({ ...GOOD, pin: " 411007 " }), "the PIN is trimmed before it is checked").toEqual({});
  });

  it("reports every bad field at once", () => {
    expect(Object.keys(validate({ name: "", phone: "123", pin: "" })).sort(), "all three fields are wrong").toEqual(["name", "phone", "pin"]);
  });
});

describe("setupStitchCounter", () => {
  it("shows 0/12 at the start", () => {
    setupStitchCounter($("#stitch"), $("#stitch-count"), 12);
    expect($("#stitch-count").textContent, "an empty box should show 0/12").toBe("0/12");
  });

  it("counts the letters as the customer types", () => {
    setupStitchCounter($("#stitch"), $("#stitch-count"), 12);
    type("Aarav");
    expect($("#stitch-count").textContent, "Aarav has 5 letters, so it should show 5/12").toBe("5/12");
  });

  it("marks the count when the name is too long", () => {
    setupStitchCounter($("#stitch"), $("#stitch-count"), 12);
    type("Aarav Kulkarni");
    expect($("#stitch-count").textContent, "Aarav Kulkarni is 14 characters, so it should show 14/12").toBe("14/12");
    expect($("#stitch-count").classList.contains("over"), "above 12 the counter should get the class over").toBe(true);
  });

  it("removes the mark once the name is short enough again", () => {
    setupStitchCounter($("#stitch"), $("#stitch-count"), 12);
    type("Aarav Kulkarni");
    type("Aarav K");
    expect($("#stitch-count").classList.contains("over"), "at 7 characters the class over should be gone").toBe(false);
  });
});

describe("setupOrderForm", () => {
  it("stops the page from reloading", () => {
    setupOrderForm($("#order"), vi.fn());
    fill(GOOD);
    expect(submit(), "the submit handler should call event.preventDefault()").toBe(false);
  });

  it("sends clean details when everything is fine", () => {
    const onOrder = vi.fn();
    setupOrderForm($("#order"), onOrder);
    fill({ name: "  Anjali Deshpande ", phone: "98765 43210", pin: "411007" });
    submit();
    expect(onOrder.mock.calls, "onOrder should get the name trimmed and the phone without spaces, once").toEqual([
      [{ name: "Anjali Deshpande", phone: "9876543210", pin: "411007" }],
    ]);
  });

  it("does not send an order with a bad phone number", () => {
    const onOrder = vi.fn();
    setupOrderForm($("#order"), onOrder);
    fill({ ...GOOD, phone: "98765 4321" });
    submit();
    expect(onOrder.mock.calls.length, "with a 9-digit phone, onOrder must not be called").toBe(0);
  });

  it("shows the message next to the bad field and marks it invalid", () => {
    setupOrderForm($("#order"), vi.fn());
    fill({ ...GOOD, phone: "98765 4321" });
    submit();
    expect($('[data-error-for="phone"]').textContent, "the phone message should say Enter a 10-digit phone number.").toBe("Enter a 10-digit phone number.");
    expect($("#phone").getAttribute("aria-invalid"), "the phone input should have aria-invalid=\"true\"").toBe("true");
    expect($('[data-error-for="name"]').textContent, "the name is fine, so its message should be empty").toBe("");
    expect($("#name").getAttribute("aria-invalid"), "the name input should have aria-invalid=\"false\"").toBe("false");
  });

  it("clears the message once the mistake is fixed", () => {
    const onOrder = vi.fn();
    setupOrderForm($("#order"), onOrder);
    fill({ ...GOOD, pin: "41100" });
    submit();
    fill({ pin: "411007" });
    submit();
    expect($('[data-error-for="pin"]').textContent, "after fixing the PIN, its message should be gone").toBe("");
    expect($("#pin").getAttribute("aria-invalid"), "after fixing the PIN, aria-invalid should be \"false\"").toBe("false");
    expect(onOrder.mock.calls.length, "the fixed form should send the order once").toBe(1);
  });
});
