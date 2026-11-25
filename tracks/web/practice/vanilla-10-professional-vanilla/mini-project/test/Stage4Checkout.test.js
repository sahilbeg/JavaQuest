import { describe, it, expect, beforeEach, vi } from "vitest";
import { addressProblem, isValidUpiId, mountCheckout } from "../src/checkout.js";

const good = { name: "Meera Kulkarni", phone: "98765 43210", pin: "411007" };

describe("Stage 4: addressProblem", () => {
  it("accepts a good address", () => {
    expect(addressProblem(good), "a name, 98765 43210 and 411007 are all fine").toBe("");
  });

  it("asks for a name first", () => {
    expect(addressProblem({ ...good, name: "   " }), "a blank name should give Enter your name.").toBe("Enter your name.");
    expect(addressProblem({ name: "", phone: "", pin: "" }), "with everything missing, the name comes first").toBe("Enter your name.");
  });

  it("needs a 10-digit phone number, with spaces allowed", () => {
    expect(addressProblem({ ...good, phone: "98765" }), "5 digits is too short").toBe("Enter a 10-digit phone number.");
    expect(addressProblem({ ...good, phone: "98765 4321O" }), "a letter O is not the digit 0").toBe("Enter a 10-digit phone number.");
    expect(addressProblem({ ...good, phone: "9876543210" }), "10 digits without a space is fine too").toBe("");
  });

  it("needs a 6-digit PIN code", () => {
    expect(addressProblem({ ...good, pin: "41100" }), "5 digits is not a PIN code").toBe("Enter a 6-digit PIN code.");
  });
});

describe("Stage 4: isValidUpiId", () => {
  it("accepts real-looking UPI IDs", () => {
    expect(isValidUpiId("meera@okaxis"), "meera@okaxis is a good UPI ID").toBe(true);
    expect(isValidUpiId("raju.pawar-1@ybl"), "dots, dashes and digits are allowed before the @").toBe(true);
  });

  it("refuses things that aren't UPI IDs", () => {
    expect(isValidUpiId("meera"), "there's no @").toBe(false);
    expect(isValidUpiId("@okaxis"), "there's nothing before the @").toBe(false);
    expect(isValidUpiId("m@okaxis"), "one character before the @ is too short").toBe(false);
    expect(isValidUpiId("meera@ok1"), "the bank part must be letters only").toBe(false);
  });
});

describe("Stage 4: the checkout steps", () => {
  let section;
  let onPlaced;
  let onBack;

  beforeEach(() => {
    document.body.innerHTML = `<section id="checkout"></section>`;
    section = document.querySelector("#checkout");
    onPlaced = vi.fn();
    onBack = vi.fn();
    mountCheckout(section, { totalPaise: 51000, orderId: "order-17", onPlaced, onBack });
  });

  const heading = () => section.querySelector("#checkout-heading");
  const alertText = () => section.querySelector('[role="alert"]')?.textContent;
  const currentStep = () => [...section.querySelectorAll(".steps li")].filter((li) => li.getAttribute("aria-current") === "step").map((li) => li.textContent);

  function field(labelText) {
    const label = [...section.querySelectorAll("label")].find((element) => element.textContent === labelText);
    return label ? document.getElementById(label.htmlFor) : null;
  }

  function button(text) {
    return [...section.querySelectorAll("button")].find((element) => element.textContent === text) ?? null;
  }

  function fillAddress({ name, phone, pin }) {
    field("Name").value = name;
    field("Phone").value = phone;
    field("PIN code").value = pin;
    button("Continue")?.click();
  }

  function pay(upiId) {
    field("UPI ID").value = upiId;
    button("Pay ₹510.00")?.click();
  }

  it("starts on the address step, with focus on its heading", () => {
    expect(heading()?.textContent, "the first step's heading is Delivery address").toBe("Delivery address");
    expect(document.activeElement === heading(), "focus should start on the heading, so a screen reader begins there").toBe(true);
    expect(currentStep(), "only the Address step should have aria-current=\"step\"").toEqual(["Address"]);
  });

  it("shows the first problem in the alert and stays on the address step", () => {
    fillAddress({ ...good, name: "  " });
    expect(alertText(), "the alert should say Enter your name.").toBe("Enter your name.");
    expect(heading()?.textContent, "with a problem, checkout should stay on Delivery address").toBe("Delivery address");
  });

  it("moves to payment with a good address, and focus follows", () => {
    fillAddress(good);
    expect(heading()?.textContent, "a good address should lead to Pay by UPI").toBe("Pay by UPI");
    expect(document.activeElement === heading(), "focus should move to the new step's heading").toBe(true);
    expect(currentStep(), "only the Payment step should be current now").toEqual(["Payment"]);
    expect(alertText(), "there should be no error on a new step").toBe("");
  });

  it("shows the real total on the pay button", () => {
    fillAddress(good);
    expect(button("Pay ₹510.00"), "the button should read Pay ₹510.00 for 51000 paise").not.toBeNull();
  });

  it("refuses a bad UPI ID", () => {
    fillAddress(good);
    pay("meera");
    expect(alertText(), "a bad UPI ID should show the UPI message").toBe("Enter a UPI ID like name@bank.");
    expect(onPlaced.mock.calls.length, "no order should be placed with a bad UPI ID").toBe(0);
    expect(heading()?.textContent, "checkout should stay on Pay by UPI").toBe("Pay by UPI");
  });

  it("places the order once with a good UPI ID, and says thank you", () => {
    fillAddress(good);
    pay("meera@okaxis");
    expect(onPlaced.mock.calls, "onPlaced should be called once with the order").toEqual([[{ orderId: "order-17", totalPaise: 51000, payment: "UPI" }]]);
    expect(heading()?.textContent, "the last step's heading is Thank you!").toBe("Thank you!");
    expect(document.activeElement === heading(), "focus should move to Thank you!").toBe(true);
    expect(currentStep(), "only the Done step should be current").toEqual(["Done"]);
    expect(section.textContent.includes("order-17"), "the thank-you step should show the order id").toBe(true);
  });

  it("goes back to the shop from the address step", () => {
    button("Back to the shop")?.click();
    expect(onBack.mock.calls.length, "Back to the shop should call onBack once").toBe(1);
  });
});
