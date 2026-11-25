import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CounterPad } from "../src/CounterPad.tsx";

afterEach(cleanup);

function setup() {
  const user = userEvent.setup();
  const onAdd = vi.fn();
  const { container } = render(<CounterPad onAdd={onAdd} />);
  const box = screen.queryByLabelText("Item code") as HTMLInputElement | null;
  const add = screen.queryByRole("button", { name: "Add" });
  const message = () => container.querySelector(".message")?.textContent;
  return { user, onAdd, box: box!, add: add!, message };
}

describe("CounterPad", () => {
  it("keeps the code in capitals as you type", async () => {
    const { user, box } = setup();
    await user.type(box, "ami");
    expect(box.value, "typing ami should show AMI in the box").toBe("AMI");
  });

  it("adds the item when you press Enter", async () => {
    const { user, onAdd, box, message } = setup();
    await user.type(box, "ami{Enter}");
    expect(onAdd.mock.calls.length, "pressing Enter should call onAdd once").toBe(1);
    expect(onAdd.mock.calls[0]?.[0], "onAdd should get the code AMI").toBe("AMI");
    expect(message(), "the message should say Added Amigurumi").toBe("Added Amigurumi");
    expect(box.value, "the box should be empty again, ready for the next code").toBe("");
  });

  it("adds the item when you press the Add button", async () => {
    const { user, onAdd, box, add, message } = setup();
    await user.type(box, "bag");
    await user.click(add);
    expect(onAdd.mock.calls.map((call) => call[0]).join(","), "the Add button should call onAdd with BAG").toBe("BAG");
    expect(message(), "the message should say Added Tote bag").toBe("Added Tote bag");
  });

  it("ignores spaces around the code", async () => {
    const { user, onAdd, box } = setup();
    await user.type(box, "  key {Enter}");
    expect(onAdd.mock.calls.map((call) => call[0]).join(","), "  key  with spaces should still add KEY").toBe("KEY");
  });

  it("refuses a code it does not know", async () => {
    const { user, onAdd, box, message } = setup();
    await user.type(box, "xyz{Enter}");
    expect(onAdd.mock.calls.length, "an unknown code should not call onAdd").toBe(0);
    expect(message(), "the message should say Unknown code: XYZ").toBe("Unknown code: XYZ");
    expect(box.value, "the mistyped code should stay in the box so Raju can fix it").toBe("XYZ");
  });

  it("asks for a code when the box is empty", async () => {
    const { user, onAdd, add, message } = setup();
    await user.click(add);
    expect(onAdd.mock.calls.length, "an empty box should not call onAdd").toBe(0);
    expect(message(), "the message should say Type a code first").toBe("Type a code first");
  });

  it("does not add anything until Enter or Add", async () => {
    const { user, onAdd, box } = setup();
    await user.type(box, "am");
    expect(onAdd.mock.calls.length, "typing letters alone should not call onAdd").toBe(0);
  });
});
