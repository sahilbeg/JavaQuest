import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { listenForAdds, listenForLineActions } from "../src/delegate.js";

beforeEach(() => {
  document.body.innerHTML = `
    <ul id="products">
      <li class="product" data-id="DC-1042">
        <h3>Golu the elephant</h3>
        <button class="add" type="button"><span class="icon">🛒</span> <span class="label">Add to cart</span></button>
      </li>
      <li class="product" data-id="DC-1013">
        <h3>Rani pink keychain</h3>
        <button class="add" type="button"><span class="icon">🛒</span> <span class="label">Add to cart</span></button>
      </li>
    </ul>
    <ul id="lines">
      <li class="line" data-id="DC-1042">
        <span class="name">Golu the elephant</span>
        <button type="button" data-action="dec">−</button>
        <span class="qty">2</span>
        <button type="button" data-action="inc">+</button>
        <button type="button" data-action="remove">Remove</button>
        <button type="button" data-action="gift">Gift wrap</button>
      </li>
    </ul>`;
});

afterEach(() => {
  vi.restoreAllMocks();
});

const products = () => document.querySelector("#products");
const lines = () => document.querySelector("#lines");
const inCard = (id, selector) => document.querySelector(`.product[data-id="${id}"] ${selector}`);
const lineButton = (action) => document.querySelector(`#lines [data-action="${action}"]`);

function newCard(id, name) {
  const card = document.createElement("li");
  card.className = "product";
  card.dataset.id = id;
  const title = document.createElement("h3");
  title.textContent = name;
  const button = document.createElement("button");
  button.className = "add";
  button.type = "button";
  button.textContent = "Add to cart";
  card.append(title, button);
  return card;
}

describe("listenForAdds", () => {
  it("calls onAdd with the card's id when its Add button is clicked", () => {
    const onAdd = vi.fn();
    listenForAdds(products(), onAdd);
    inCard("DC-1013", ".add").click();
    expect(onAdd.mock.calls, "a click on the keychain's Add button should call onAdd(\"DC-1013\") once").toEqual([["DC-1013"]]);
  });

  it("works when the click lands on the icon inside the button", () => {
    const onAdd = vi.fn();
    listenForAdds(products(), onAdd);
    inCard("DC-1042", ".icon").click();
    expect(onAdd.mock.calls, "a click on the cart icon inside Golu's button should call onAdd(\"DC-1042\"): use closest").toEqual([["DC-1042"]]);
  });

  it("ignores clicks that are not on an Add button", () => {
    const onAdd = vi.fn();
    listenForAdds(products(), onAdd);
    inCard("DC-1042", "h3").click();
    expect(onAdd.mock.calls.length, "a click on a heading should not add anything").toBe(0);
  });

  it("works for a card added after the listener", () => {
    const onAdd = vi.fn();
    listenForAdds(products(), onAdd);
    products().append(newCard("DC-1020", "Mint baby booties"));
    inCard("DC-1020", ".add").click();
    expect(onAdd.mock.calls, "the new booties card's button should call onAdd(\"DC-1020\")").toEqual([["DC-1020"]]);
  });

  it("still works after the whole list is redrawn", () => {
    const onAdd = vi.fn();
    listenForAdds(products(), onAdd);
    products().replaceChildren(newCard("DC-1007", "Indigo tote bag"));
    inCard("DC-1007", ".add").click();
    expect(onAdd.mock.calls, "after a redraw, the tote bag's button should call onAdd(\"DC-1007\")").toEqual([["DC-1007"]]);
  });

  it("adds exactly one listener, on the list", () => {
    const listSpy = vi.spyOn(products(), "addEventListener");
    const buttonSpies = [...products().querySelectorAll(".add")].map((button) => vi.spyOn(button, "addEventListener"));
    listenForAdds(products(), vi.fn());
    expect(listSpy.mock.calls.length, "listenForAdds should add one listener to the list").toBe(1);
    expect(listSpy.mock.calls[0]?.[0], "the list's one listener should be for \"click\"").toBe("click");
    const onButtons = buttonSpies.reduce((sum, spy) => sum + spy.mock.calls.length, 0);
    expect(onButtons, "the buttons themselves should get no listeners: let clicks bubble up to the list").toBe(0);
  });
});

describe("listenForLineActions", () => {
  const setup = () => {
    const actions = { inc: vi.fn(), dec: vi.fn(), remove: vi.fn() };
    listenForLineActions(lines(), actions);
    return actions;
  };

  it("runs inc with the line's id when + is clicked", () => {
    const actions = setup();
    lineButton("inc").click();
    expect(actions.inc.mock.calls, "+ on Golu's line should call actions.inc(\"DC-1042\")").toEqual([["DC-1042"]]);
    expect(actions.dec.mock.calls.length, "+ should not call dec").toBe(0);
  });

  it("runs dec when − is clicked", () => {
    const actions = setup();
    lineButton("dec").click();
    expect(actions.dec.mock.calls, "− on Golu's line should call actions.dec(\"DC-1042\")").toEqual([["DC-1042"]]);
  });

  it("runs remove when Remove is clicked", () => {
    const actions = setup();
    lineButton("remove").click();
    expect(actions.remove.mock.calls, "Remove on Golu's line should call actions.remove(\"DC-1042\")").toEqual([["DC-1042"]]);
  });

  it("ignores a click on the line's text", () => {
    const actions = setup();
    document.querySelector("#lines .name").click();
    const total = actions.inc.mock.calls.length + actions.dec.mock.calls.length + actions.remove.mock.calls.length;
    expect(total, "a click on the item's name should run no action").toBe(0);
  });

  it("ignores a button whose action is unknown", () => {
    const actions = setup();
    lineButton("gift").click();
    const total = actions.inc.mock.calls.length + actions.dec.mock.calls.length + actions.remove.mock.calls.length;
    expect(total, "gift is not in actions, so nothing should run").toBe(0);
  });

  it("works for a line added after the listener", () => {
    const actions = setup();
    const line = document.createElement("li");
    line.className = "line";
    line.dataset.id = "DC-1001";
    const plus = document.createElement("button");
    plus.type = "button";
    plus.dataset.action = "inc";
    plus.textContent = "+";
    line.append(plus);
    lines().append(line);
    plus.click();
    expect(actions.inc.mock.calls, "+ on the new coaster line should call actions.inc(\"DC-1001\")").toEqual([["DC-1001"]]);
  });
});
