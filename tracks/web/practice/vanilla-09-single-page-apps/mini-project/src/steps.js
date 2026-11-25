// The four checkout screens. This file is finished.
// Each view gets { state, store, go, total, errors } and returns a <section data-step="..."> for the outlet.

export const STEP_TITLES = { cart: "Your cart", details: "Your details", pay: "Pay by UPI", done: "Order placed" };

const NAMES = { "DC-1042": "Golu the elephant", "DC-2001": "Marigold coaster set", "DC-3001": "Indigo tote bag" };

function rupees(paise) {
  return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(paise / 100);
}

function el(tag, text) {
  const element = document.createElement(tag);
  element.textContent = text;
  return element;
}

function screen(step) {
  const section = document.createElement("section");
  section.dataset.step = step;
  section.append(el("h2", STEP_TITLES[step]));
  return section;
}

function button(text, onClick) {
  const b = el("button", text);
  b.type = "button";
  b.addEventListener("click", onClick);
  return b;
}

function cartView({ state, go, total }) {
  const section = screen("cart");
  if (state.lines.length === 0) {
    section.append(el("p", "Your cart is empty."));
    return section;
  }
  const list = document.createElement("ul");
  for (const line of state.lines) {
    list.append(el("li", `${NAMES[line.id] ?? line.id} × ${line.qty}`));
  }
  section.append(list, el("p", `Total with delivery: ${rupees(total)}`), button("Continue", () => go("details")));
  return section;
}

function detailsView({ state, store, go, errors }) {
  const section = screen("details");
  const form = document.createElement("form");
  const typed = Object.values(state.details).some((value) => value !== "");
  for (const [name, label] of [["name", "Your name"], ["phone", "Phone number"], ["pin", "PIN code"]]) {
    const input = document.createElement("input");
    input.name = name;
    input.value = state.details[name];
    input.setAttribute("aria-label", label);
    form.append(input, el("p", typed ? errors[name] ?? "" : ""));
  }
  const submit = el("button", "Continue to payment");
  submit.type = "submit";
  form.append(submit);
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    store.setState((s) => ({ ...s, details: { name: data.name, phone: data.phone, pin: data.pin } }));
    go("pay");
  });
  section.append(form);
  return section;
}

function payView({ store, go, total }) {
  const section = screen("pay");
  section.append(
    el("p", `Pay ${rupees(total)} by UPI to dhaaga@upi, then tap the button.`),
    button("I have paid", () => {
      store.setState((s) => ({ ...s, paid: true }));
      go("done");
    }),
  );
  return section;
}

function doneView() {
  const section = screen("done");
  section.append(el("p", "Thank you! Meera Aunty is packing your order."));
  return section;
}

export const STEP_VIEWS = { cart: cartView, details: detailsView, pay: payView, done: doneView };
