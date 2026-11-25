import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PincodeBoxes } from "../src/PincodeBoxes.tsx";

afterEach(cleanup);

function box(n: number) {
  return screen.getByLabelText(`Digit ${n}`) as HTMLInputElement;
}

/** Which box has the focus, as "Digit 3", or what has it instead. */
function focused() {
  return document.activeElement?.getAttribute("aria-label") ?? document.activeElement?.tagName;
}

describe("PincodeBoxes", () => {
  it("starts with the cursor in the first box", () => {
    render(<PincodeBoxes onComplete={() => {}} />);
    expect(focused(), "after the boxes appear, Digit 1 should have the focus (use a ref and an effect)").toBe("Digit 1");
  });

  it("moves to the next box after each digit", async () => {
    const user = userEvent.setup();
    render(<PincodeBoxes onComplete={() => {}} />);
    await user.click(box(1));
    await user.keyboard("4");
    expect(box(1).value, "Digit 1 should hold the 4").toBe("4");
    expect(focused(), "after typing a digit, the focus should move to Digit 2").toBe("Digit 2");
    await user.keyboard("1");
    expect(focused(), "after the second digit, the focus should move to Digit 3").toBe("Digit 3");
  });

  it("lets you type the whole PIN code without touching another box", async () => {
    const user = userEvent.setup();
    const onComplete = vi.fn();
    render(<PincodeBoxes onComplete={onComplete} />);
    await user.click(box(1));
    await user.keyboard("411007");
    expect([1, 2, 3, 4, 5, 6].map((n) => box(n).value).join(""), "the six boxes should hold 4, 1, 1, 0, 0, 7").toBe("411007");
    expect(onComplete.mock.calls.length, "a full PIN code should call onComplete once").toBe(1);
    expect(onComplete.mock.calls[0]?.[0], "onComplete should get the six digits joined").toBe("411007");
  });

  it("ignores a letter", async () => {
    const user = userEvent.setup();
    render(<PincodeBoxes onComplete={() => {}} />);
    await user.click(box(1));
    await user.keyboard("a");
    expect(box(1).value, "a letter isn't a digit, so Digit 1 should stay empty").toBe("");
    expect(focused(), "a letter shouldn't move the focus").toBe("Digit 1");
  });

  it("goes back one box when you press Backspace in an empty box", async () => {
    const user = userEvent.setup();
    render(<PincodeBoxes onComplete={() => {}} />);
    await user.click(box(1));
    await user.keyboard("41");
    expect(focused(), "after two digits the focus should be on Digit 3").toBe("Digit 3");
    await user.keyboard("{Backspace}");
    expect(focused(), "Backspace in the empty Digit 3 should move the focus back to Digit 2").toBe("Digit 2");
    expect(box(2).value, "moving back shouldn't delete Digit 2's 1 as well (call event.preventDefault())").toBe("1");
  });

  it("stays in the first box when you press Backspace there", async () => {
    const user = userEvent.setup();
    render(<PincodeBoxes onComplete={() => {}} />);
    await user.click(box(1));
    await user.keyboard("{Backspace}");
    expect(focused(), "there is no box before Digit 1, so the focus should stay there").toBe("Digit 1");
  });
});
