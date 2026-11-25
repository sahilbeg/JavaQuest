import { describe, it, expect, beforeEach } from "vitest";
import { required, matches, checkoutRules, validate, showErrors, focusFirstError } from "../src/validation.js";

const NAME = "Please tell us your name.";
const PHONE_MISSING = "Please give a phone number.";
const PHONE_SHAPE = "A phone number has 10 digits, like 98765 43210.";
const PIN_MISSING = "Please give your PIN code.";
const PIN_SHAPE = "A PIN code has 6 digits, like 411004.";
const ADDRESS = "Please give your address.";

const good = { name: "Asha Kulkarni", phone: "98765 43210", pin: "411004", address: "12 Lane 5, Koregaon Park, Pune" };

describe("required", () => {
  const rule = required("Please fill this in.");

  it("fails an empty value", () => {
    expect(rule(""), "an empty value should give the message").toBe("Please fill this in.");
  });

  it("fails a value that is only spaces", () => {
    expect(rule("   "), "spaces only count as empty; use value.trim()").toBe("Please fill this in.");
  });

  it("passes a value with text in it", () => {
    expect(rule("Asha"), "\"Asha\" is filled in, so the rule should give null").toBe(null);
  });
});

describe("matches", () => {
  const rule = matches(/^\d{5} ?\d{5}$/, "Ten digits, please.");

  it("passes a value that fits the pattern", () => {
    expect(rule("98765 43210"), "\"98765 43210\" fits the phone pattern, so the answer is null").toBe(null);
    expect(rule("9876543210"), "the space in the middle is optional").toBe(null);
  });

  it("fails a value that doesn't fit", () => {
    expect(rule("12345"), "five digits don't fit, so the rule should give its message").toBe("Ten digits, please.");
  });

  it("ignores spaces at the ends", () => {
    expect(rule("  98765 43210 "), "spaces around the value should be trimmed before testing").toBe(null);
  });
});

describe("validate", () => {
  it("gives {} when every field is fine", () => {
    expect(validate(good, checkoutRules), "a fully filled-in form has no errors").toEqual({});
  });

  it("gives every field's first message for an empty form", () => {
    const empty = { name: "", phone: "", pin: "", address: "" };
    expect(validate(empty, checkoutRules), "each empty field should get its required message, and only that one").toEqual({
      name: NAME,
      phone: PHONE_MISSING,
      pin: PIN_MISSING,
      address: ADDRESS,
    });
  });

  it("reports only the fields that fail", () => {
    const values = { ...good, phone: "98765", pin: "011004" };
    expect(validate(values, checkoutRules), "a short phone and a PIN starting with 0 should give the two shape messages").toEqual({
      phone: PHONE_SHAPE,
      pin: PIN_SHAPE,
    });
  });

  it("treats a missing value as empty", () => {
    const values = { phone: "98765 43210", pin: "411004", address: "Aundh, Pune" };
    expect(validate(values, checkoutRules), "with no name in the values, name should get its required message").toEqual({ name: NAME });
  });

  it("works with any form's rules", () => {
    const giftRules = { message: [required("Write a short note for the gift card.")] };
    expect(validate({ message: "" }, giftRules), "validate must use the rules it is given, not only the checkout's").toEqual({
      message: "Write a short note for the gift card.",
    });
  });
});

describe("on the page", () => {
  let form;

  beforeEach(() => {
    document.body.innerHTML = `
      <form id="checkout" novalidate>
        <input id="name" name="name" aria-describedby="name-error"><p id="name-error"></p>
        <input id="phone" name="phone" aria-describedby="phone-error"><p id="phone-error"></p>
        <input id="pin" name="pin" aria-describedby="pin-error"><p id="pin-error"></p>
        <textarea id="address" name="address" aria-describedby="address-error"></textarea><p id="address-error"></p>
        <button type="submit">Place order</button>
      </form>
    `;
    form = document.querySelector("#checkout");
  });

  const text = (id) => document.querySelector(`#${id}`).textContent;
  const invalid = (id) => document.querySelector(`#${id}`).getAttribute("aria-invalid");

  it("shows each message under its field and marks the field invalid", () => {
    showErrors(form, { phone: PHONE_SHAPE });
    expect(text("phone-error"), "the phone message should appear in #phone-error").toBe(PHONE_SHAPE);
    expect(invalid("phone"), "the phone input should get aria-invalid=\"true\"").toBe("true");
  });

  it("marks the fields without errors as valid", () => {
    showErrors(form, { phone: PHONE_SHAPE });
    expect(invalid("name"), "a field with no error should get aria-invalid=\"false\"").toBe("false");
    expect(invalid("address"), "the textarea counts as a field too").toBe("false");
    expect(text("name-error"), "a field with no error should have an empty message").toBe("");
  });

  it("clears old messages once a field is fixed", () => {
    showErrors(form, { name: NAME, phone: PHONE_SHAPE });
    showErrors(form, { phone: PHONE_SHAPE });
    expect(text("name-error"), "the name was fixed, so its old message must go").toBe("");
    expect(invalid("name"), "the fixed name should be marked valid again").toBe("false");
  });

  it("moves focus to the first field with an error, in form order", () => {
    const first = focusFirstError(form, { pin: PIN_SHAPE, phone: PHONE_SHAPE });
    expect(first, "phone comes before pin in the form, so phone is first").toBe("phone");
    expect(document.activeElement?.id, "the phone input should have the focus").toBe("phone");
  });

  it("leaves focus alone when there are no errors", () => {
    document.querySelector("#name").focus();
    expect(focusFirstError(form, {}), "with no errors there is no field to focus, so the answer is null").toBe(null);
    expect(document.activeElement?.id, "focus should stay where it was").toBe("name");
  });
});
