import { describe, it, expect, beforeEach, vi } from "vitest";
import { clampQty, findInput, setupStepper } from "../src/stepper.ts";

beforeEach(() => {
  document.body.innerHTML = `
    <div class="stepper">
      <button type="button" data-step="-1">-</button>
      <input name="qty" type="number" value="1">
      <button type="button" data-step="1">+</button>
      <button type="button" class="info">?</button>
      <span class="note">Max 10</span>
    </div>`;
});

function stepper(): HTMLElement {
  return document.querySelector(".stepper") as HTMLElement;
}
function box(): HTMLInputElement {
  return document.querySelector("input") as HTMLInputElement;
}
function plus(): HTMLButtonElement {
  return document.querySelector('[data-step="1"]') as HTMLButtonElement;
}
function minus(): HTMLButtonElement {
  return document.querySelector('[data-step="-1"]') as HTMLButtonElement;
}
function type(text: string): void {
  box().value = text;
  box().dispatchEvent(new Event("input", { bubbles: true }));
}

describe("clampQty", () => {
  it("keeps a quantity from 1 to 10 as it is", () => {
    expect(clampQty(3), "3 is a fine quantity").toBe(3);
    expect(clampQty(10), "10 is the most allowed").toBe(10);
  });

  it("raises anything below 1 to 1, and lowers anything above 10 to 10", () => {
    expect(clampQty(0), "0 should become 1").toBe(1);
    expect(clampQty(-4), "-4 should become 1").toBe(1);
    expect(clampQty(37), "37 should become 10").toBe(10);
  });

  it("rounds down and turns NaN into 1", () => {
    expect(clampQty(2.7), "2.7 should round down to 2").toBe(2);
    expect(clampQty(Number.NaN), "NaN (from letters) should become 1").toBe(1);
  });
});

describe("findInput", () => {
  it("finds the input with that name", () => {
    expect(findInput(document.body, "qty"), "findInput should return the qty box on the page").toBe(box());
  });

  it("throws a clear error when the input is missing", () => {
    expect(() => findInput(document.body, "pincode"), "there is no pincode box, so findInput should throw").toThrow("No input named pincode");
  });
});

describe("setupStepper", () => {
  it("adds one when + is clicked", () => {
    const onChange = vi.fn();
    setupStepper(stepper(), onChange);
    plus().click();
    expect(box().value, "the box should now show 2").toBe("2");
    expect(onChange.mock.calls, "onChange should be called once, with 2").toEqual([[2]]);
  });

  it("never goes below 1", () => {
    const onChange = vi.fn();
    setupStepper(stepper(), onChange);
    minus().click();
    expect(box().value, "1 minus 1 should stay at 1").toBe("1");
    expect(onChange.mock.calls, "onChange should get 1").toEqual([[1]]);
  });

  it("never goes above 10", () => {
    const onChange = vi.fn();
    setupStepper(stepper(), onChange);
    for (let i = 0; i < 12; i++) {
      plus().click();
    }
    expect(box().value, "twelve clicks on + should stop at 10").toBe("10");
    expect(onChange.mock.calls.at(-1), "the last quantity passed on should be 10").toEqual([10]);
  });

  it("ignores clicks on things that aren't step buttons", () => {
    const onChange = vi.fn();
    setupStepper(stepper(), onChange);
    (document.querySelector(".note") as HTMLElement).click();
    (document.querySelector(".info") as HTMLButtonElement).click();
    expect(onChange.mock.calls.length, "clicking the note or a button without data-step should do nothing").toBe(0);
    expect(box().value, "the box should still show 1").toBe("1");
  });

  it("uses a typed quantity", () => {
    const onChange = vi.fn();
    setupStepper(stepper(), onChange);
    type("7");
    expect(onChange.mock.calls, "typing 7 should pass 7 on").toEqual([[7]]);
  });

  it("clamps a typed quantity and writes it back", () => {
    const onChange = vi.fn();
    setupStepper(stepper(), onChange);
    type("25");
    expect(box().value, "typing 25 should change the box to 10").toBe("10");
    expect(onChange.mock.calls, "onChange should get 10").toEqual([[10]]);
  });

  it("throws when the stepper has no qty box", () => {
    document.body.innerHTML = `<div class="stepper"><button data-step="1">+</button></div>`;
    expect(() => setupStepper(stepper(), () => {}), "a stepper with no qty box should throw").toThrow("No input named qty");
  });
});
