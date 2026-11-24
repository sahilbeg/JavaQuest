import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

/** The custom order form, or null. */
const form = (doc) => doc.querySelector('form[action="/custom-orders"]');

/** The field with this id inside the form, or null. */
const field = (doc, id) => form(doc)?.querySelector(`#${id}`) ?? null;

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

const COLOURS = ["marigold", "indigo", "rani-pink", "mint", "off-white"];

describe("The custom order request form", () => {
  it("asks for the customer's name, and won't send without it", () => {
    const doc = page();
    const name = field(doc, "name");
    expect(name?.tagName, 'the form should have an <input> with id="name"').toBe("INPUT");
    expect(name.type, "the name field should be a text input").toBe("text");
    expect(name.getAttribute("name"), 'the name field needs name="name"').toBe("name");
    expect(name.hasAttribute("required"), "the name field should be required").toBe(true);
    expect(labelOf(doc, name) ? text(labelOf(doc, name)) : "", 'the name field should have the label "Your name"').toBe("Your name");
  });

  it("asks for a phone number of exactly ten digits", () => {
    const doc = page();
    const phone = field(doc, "phone");
    expect(phone?.tagName, 'the form should have an <input> with id="phone"').toBe("INPUT");
    expect(phone.getAttribute("type"), 'the phone field should be type="tel"').toBe("tel");
    expect(phone.getAttribute("name"), 'the phone field needs name="phone"').toBe("phone");
    expect(phone.hasAttribute("required"), "the phone field should be required").toBe(true);
    const pattern = phone.getAttribute("pattern") ?? "";
    expect(fits(pattern, "9876543210"), "the pattern should accept ten digits, like 9876543210").toBe(true);
    expect(fits(pattern, "98765"), "the pattern should refuse five digits").toBe(false);
    expect(fits(pattern, "98765 43210"), "the pattern should refuse a space").toBe(false);
    expect(fits(pattern, "98765abcde"), "the pattern should refuse letters").toBe(false);
    expect(labelOf(doc, phone) ? text(labelOf(doc, phone)) : "", 'the phone field should have the label "Phone number"').toBe("Phone number");
  });

  it("lets the customer pick one item from a required select", () => {
    const doc = page();
    const item = field(doc, "item");
    expect(item?.tagName, 'the form should have a <select> with id="item"').toBe("SELECT");
    expect(item.getAttribute("name"), 'the select needs name="item"').toBe("item");
    expect(item.hasAttribute("required"), "the select should be required").toBe(true);
    const values = Array.from(item.querySelectorAll("option")).map((o) => o.getAttribute("value"));
    expect(values, 'the options should have the values "", CST, KEY, AMI, BAG and BTY, in that order').toEqual(["", "CST", "KEY", "AMI", "BAG", "BTY"]);
    expect(labelOf(doc, item) ? text(labelOf(doc, item)) : "", 'the select should have the label "Item"').toBe("Item");
  });

  it("offers the five colours as one required group of radio buttons", () => {
    const doc = page();
    const fieldset = form(doc)?.querySelector("fieldset");
    expect(fieldset, "the form should have a <fieldset> for the colours").toBeTruthy();
    const legend = fieldset.querySelector("legend");
    expect(legend ? text(legend) : "", "the fieldset should have the legend Colour").toBe("Colour");
    const radios = Array.from(fieldset.querySelectorAll('input[type="radio"]'));
    expect(radios.map((r) => r.getAttribute("value")), "there should be five radio buttons: marigold, indigo, rani-pink, mint, off-white").toEqual(COLOURS);
    for (const radio of radios) {
      expect(radio.getAttribute("name"), `the ${radio.value} radio should have name="colour", so all five form one group`).toBe("colour");
      const label = labelOf(doc, radio);
      expect(label ? text(label).length > 0 : false, `the ${radio.value} radio button needs its own label`).toBe(true);
    }
    expect(radios.some((r) => r.hasAttribute("required")), "put required on one radio button, so a colour must be picked").toBe(true);
  });

  it("limits the quantity to a number from 1 to 20", () => {
    const doc = page();
    const qty = field(doc, "qty");
    expect(qty?.tagName, 'the form should have an <input> with id="qty"').toBe("INPUT");
    expect(qty.getAttribute("type"), 'the quantity should be type="number"').toBe("number");
    expect(qty.getAttribute("name"), 'the quantity needs name="qty"').toBe("qty");
    expect(qty.getAttribute("min"), 'the quantity should have min="1"').toBe("1");
    expect(qty.getAttribute("max"), 'the quantity should have max="20"').toBe("20");
    expect(qty.getAttribute("value"), 'the quantity should start at value="1"').toBe("1");
    expect(labelOf(doc, qty) ? text(labelOf(doc, qty)) : "", 'the quantity should have the label "How many?"').toBe("How many?");
  });

  it("has an optional five-line notes box", () => {
    const doc = page();
    const notes = field(doc, "notes");
    expect(notes?.tagName, 'the form should have a <textarea> with id="notes"').toBe("TEXTAREA");
    expect(notes.getAttribute("name"), 'the notes box needs name="notes"').toBe("notes");
    expect(notes.getAttribute("rows"), 'the notes box should have rows="5"').toBe("5");
    expect(notes.hasAttribute("required"), "notes are optional, so the box should not be required").toBe(false);
    expect(labelOf(doc, notes) ? text(labelOf(doc, notes)) : "", 'the notes box should have the label "Notes"').toBe("Notes");
  });

  it("ends with a submit button that says Send request", () => {
    const button = form(page())?.querySelector("button");
    expect(button, "the form should have a <button>").toBeTruthy();
    expect(button.getAttribute("type"), 'the button should have type="submit"').toBe("submit");
    expect(text(button), "the button should say Send request").toBe("Send request");
  });
});
