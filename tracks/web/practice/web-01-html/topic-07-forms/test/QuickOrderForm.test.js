import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

/** The form in the Quick order section, or null. */
const form = (doc) => doc.querySelector("#order form");

/** The label joined to a field: a label whose for matches the field's id, or a label wrapped around it. */
const labelOf = (doc, field) =>
  (field.id && doc.querySelector(`label[for="${field.id}"]`)) || field.closest("label");

describe("The quick order form", () => {
  it("sends to /orders with the post method", () => {
    const f = form(page());
    expect(f, "there should be a <form> inside the Quick order section").not.toBeNull();
    expect(f.getAttribute("action"), 'the form should have action="/orders"').toBe("/orders");
    expect((f.getAttribute("method") ?? "").toLowerCase(), 'the form should have method="post"').toBe("post");
  });

  it("has a name field and a phone field, each with a name the server can read", () => {
    const f = form(page());
    expect(f, "there should be a <form> inside the Quick order section").not.toBeNull();
    const name = f.querySelector("#order-name");
    expect(name?.tagName, 'there should be an <input> with id="order-name" inside the form').toBe("INPUT");
    expect(name.type, "the name field should be a text input").toBe("text");
    expect(name.getAttribute("name"), 'the name field needs name="name", or it is never sent').toBe("name");
    const phone = f.querySelector("#order-phone");
    expect(phone?.tagName, 'there should be an <input> with id="order-phone" inside the form').toBe("INPUT");
    expect(phone.type, "the phone field should be a text input for now").toBe("text");
    expect(phone.getAttribute("name"), 'the phone field needs name="phone", or it is never sent').toBe("phone");
  });

  it("has a four-line textarea for the message", () => {
    const f = form(page());
    expect(f, "there should be a <form> inside the Quick order section").not.toBeNull();
    const message = f.querySelector("#order-message");
    expect(message?.tagName, 'there should be a <textarea> with id="order-message" inside the form').toBe("TEXTAREA");
    expect(message.getAttribute("name"), 'the textarea needs name="message"').toBe("message");
    expect(message.getAttribute("rows"), 'the textarea should have rows="4"').toBe("4");
    expect(message.value, "the textarea should start empty, with nothing between its tags").toBe("");
  });

  it("joins every field to a label that says what to type", () => {
    const doc = page();
    const wanted = {
      "order-name": "Your name",
      "order-phone": "Phone number",
      "order-message": "What would you like?",
    };
    for (const [id, words] of Object.entries(wanted)) {
      const field = doc.getElementById(id);
      expect(field, `there should be a field with id="${id}"`).not.toBeNull();
      const label = labelOf(doc, field);
      expect(label, `the field ${id} needs a <label for="${id}">`).toBeTruthy();
      expect(text(label), `the label for ${id} should say ${words}`).toBe(words);
    }
  });

  it("has a submit button that says Send order", () => {
    const f = form(page());
    expect(f, "there should be a <form> inside the Quick order section").not.toBeNull();
    const button = f.querySelector("button");
    expect(button, "there should be a <button> inside the form").not.toBeNull();
    expect(button.getAttribute("type"), 'the button should have type="submit"').toBe("submit");
    expect(text(button), "the button should say Send order").toBe("Send order");
  });
});
