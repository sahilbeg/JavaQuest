import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

/** The field with this id inside the quick order form, or null. */
const field = (doc, id) => doc.querySelector(`#order form #${id}`);

/** The label joined to a field: a label whose for matches the field's id, or a label wrapped around it. */
const labelOf = (doc, el) => (el.id && doc.querySelector(`label[for="${el.id}"]`)) || el.closest("label");

/** Whether a value fits a pattern attribute the way a browser checks it: the whole value must match. */
const fits = (pattern, value) => {
  try {
    return new RegExp(`^(?:${pattern})$`, "u").test(value);
  } catch {
    return false;
  }
};

describe("The quick order form's checks", () => {
  it("won't send without a name", () => {
    const name = field(page(), "order-name");
    expect(name, 'the form should still have the input with id="order-name"').not.toBeNull();
    expect(name.hasAttribute("required"), "the name input should be required").toBe(true);
  });

  it("asks for a phone number of exactly ten digits", () => {
    const phone = field(page(), "order-phone");
    expect(phone, 'the form should still have the input with id="order-phone"').not.toBeNull();
    expect(phone.getAttribute("type"), 'the phone input should be type="tel", so phones show a number keypad').toBe("tel");
    expect(phone.hasAttribute("required"), "the phone input should be required").toBe(true);
    const pattern = phone.getAttribute("pattern");
    expect(pattern, "the phone input needs a pattern attribute").toBeTruthy();
    expect(fits(pattern, "9876543210"), "the pattern should accept ten digits, like 9876543210").toBe(true);
    expect(fits(pattern, "98765"), "the pattern should refuse 98765, which is only five digits").toBe(false);
    expect(fits(pattern, "98765432101"), "the pattern should refuse eleven digits").toBe(false);
    expect(fits(pattern, "98765 4321"), "the pattern should refuse a space").toBe(false);
    expect(fits(pattern, "98765abcde"), "the pattern should refuse letters").toBe(false);
  });

  it("has an optional email field of type email", () => {
    const email = field(page(), "order-email");
    expect(email, 'the form should still have the input with id="order-email"').not.toBeNull();
    expect(email.getAttribute("type"), 'the email input should be type="email"').toBe("email");
    expect(email.getAttribute("name"), 'the email input should keep name="email"').toBe("email");
    expect(email.hasAttribute("required"), "email is optional, so it should not be required").toBe(false);
  });

  it("lets the customer choose the item from a required select", () => {
    const doc = page();
    const item = field(doc, "order-item");
    expect(item?.tagName, 'there should be a <select> with id="order-item" inside the form').toBe("SELECT");
    expect(item.getAttribute("name"), 'the select needs name="item"').toBe("item");
    expect(item.hasAttribute("required"), "the select should be required, so an item must be picked").toBe(true);
    const values = Array.from(item.querySelectorAll("option")).map((o) => o.getAttribute("value"));
    expect(values, 'the options should have the values "", CST, KEY, AMI, BAG and BTY, in that order').toEqual([
      "",
      "CST",
      "KEY",
      "AMI",
      "BAG",
      "BTY",
    ]);
    for (const option of item.querySelectorAll("option")) {
      expect(text(option).length > 0, `the option with value "${option.getAttribute("value")}" needs visible text`).toBe(true);
    }
    expect(labelOf(doc, item), 'the select should be joined to the label "Item"').toBeTruthy();
  });

  it("limits the quantity to a number from 1 to 10", () => {
    const doc = page();
    const qty = field(doc, "order-qty");
    expect(qty?.tagName, 'there should be an <input> with id="order-qty" inside the form').toBe("INPUT");
    expect(qty.getAttribute("type"), 'the quantity input should be type="number"').toBe("number");
    expect(qty.getAttribute("name"), 'the quantity input needs name="qty"').toBe("qty");
    expect(qty.getAttribute("min"), 'the quantity should have min="1"').toBe("1");
    expect(qty.getAttribute("max"), 'the quantity should have max="10"').toBe("10");
    expect(qty.getAttribute("value"), 'the quantity should start at value="1"').toBe("1");
    expect(labelOf(doc, qty), 'the quantity should be joined to the label "How many?"').toBeTruthy();
  });

  it("offers delivery as two radio buttons in a fieldset with a legend", () => {
    const doc = page();
    const fieldset = doc.querySelector("#order form fieldset");
    expect(fieldset, "there should be a <fieldset> inside the form").not.toBeNull();
    const legend = fieldset.querySelector("legend");
    expect(legend ? text(legend) : null, "the fieldset should have a <legend> that says Delivery").toBe("Delivery");
    const radios = Array.from(fieldset.querySelectorAll('input[type="radio"]'));
    expect(radios.length, "the fieldset should hold two radio buttons").toBe(2);
    expect(radios.map((r) => r.getAttribute("name")), 'both radio buttons should have name="delivery", so they form one group').toEqual([
      "delivery",
      "delivery",
    ]);
    expect(radios.map((r) => r.getAttribute("value")), "the radio values should be pickup and pune").toEqual(["pickup", "pune"]);
    for (const radio of radios) {
      const label = labelOf(doc, radio);
      expect(label, `the ${radio.getAttribute("value")} radio button needs its own label`).toBeTruthy();
      expect(text(label).length > 0, `the label for the ${radio.getAttribute("value")} radio button should have words in it`).toBe(true);
    }
  });
});
