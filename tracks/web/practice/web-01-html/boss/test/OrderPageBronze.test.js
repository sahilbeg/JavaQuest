import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

/** The label joined to a field: a label whose for matches the field's id, or a label wrapped around it. */
const labelOf = (doc, field) =>
  (field.id && doc.querySelector(`label[for="${field.id}"]`)) || field.closest("label");

/** Every field a customer fills in: inputs (but not buttons or hidden ones), selects and textareas. */
const fields = (form) =>
  Array.from(form.querySelectorAll("input, select, textarea")).filter(
    (el) => !["hidden", "submit", "button"].includes((el.getAttribute("type") ?? "").toLowerCase()),
  );

describe("Bronze: every field has a label", () => {
  it("keeps the four fields the orders server needs", () => {
    const form = page().querySelector('form[action="/orders"]');
    expect(form, 'the page should still have its <form action="/orders">').not.toBeNull();
    for (const id of ["name", "phone", "item", "qty"]) {
      const field = form.querySelector(`#${id}`);
      expect(field, `the form should still have the field with id="${id}"`).not.toBeNull();
      expect(field.getAttribute("name"), `the field ${id} should keep name="${id}"`).toBe(id);
    }
  });

  it("joins every field in the form to a label that has words in it", () => {
    const doc = page();
    const form = doc.querySelector('form[action="/orders"]');
    expect(form, 'the page should still have its <form action="/orders">').not.toBeNull();
    for (const field of fields(form)) {
      const label = labelOf(doc, field);
      expect(label, `the field with id="${field.id}" has no label: add <label for="${field.id}">`).toBeTruthy();
      expect(text(label).length > 0, `the label for ${field.id} is empty: write what the field is for`).toBe(true);
    }
  });

  it("uses labels that say what each field is for", () => {
    const doc = page();
    const wanted = {
      name: [/name/i, "Your name"],
      phone: [/phone/i, "Phone number"],
      item: [/item/i, "Item"],
      qty: [/how many|quantity/i, "How many?"],
    };
    for (const [id, [words, example]] of Object.entries(wanted)) {
      const field = doc.getElementById(id);
      expect(field, `the field with id="${id}" should still be on the page`).not.toBeNull();
      const label = labelOf(doc, field);
      expect(label, `the field ${id} needs a label, like ${example}`).toBeTruthy();
      expect(words.test(text(label)), `the label for ${id} should say what it is, like ${example}`).toBe(true);
    }
  });
});
