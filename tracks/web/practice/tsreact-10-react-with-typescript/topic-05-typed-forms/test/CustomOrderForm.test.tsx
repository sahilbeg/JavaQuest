import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CustomOrderForm } from "../src/CustomOrderForm.tsx";

afterEach(cleanup);

function setup() {
  const user = userEvent.setup();
  const onSend = vi.fn();
  const { container } = render(<CustomOrderForm onSend={onSend} />);
  return {
    user,
    onSend,
    name: screen.queryByLabelText("Your name") as HTMLInputElement,
    item: screen.queryByLabelText("Item") as HTMLSelectElement,
    qty: screen.queryByLabelText("Quantity") as HTMLSelectElement,
    gift: screen.queryByLabelText("Gift wrap (₹30)") as HTMLInputElement,
    send: screen.queryByRole("button", { name: "Send request" }) as HTMLButtonElement,
    total: () => container.querySelector(".total")?.textContent,
    error: () => container.querySelector(".error")?.textContent,
  };
}

describe("CustomOrderForm", () => {
  it("shows what you type in the name box", async () => {
    const { user, name } = setup();
    await user.type(name, "Asha Rao");
    expect(name.value, "the name box should show what was typed").toBe("Asha Rao");
  });

  it("starts with one amigurumi for ₹450.00", () => {
    const { total } = setup();
    expect(total(), "one amigurumi, no gift wrap, costs ₹450.00").toBe("Total: ₹450.00");
  });

  it("works out the total for the item and quantity you choose", async () => {
    const { user, item, qty, total } = setup();
    await user.selectOptions(item, "BAG");
    await user.selectOptions(qty, "2");
    expect(item.value, "the Item list should now show the tote bag").toBe("BAG");
    expect(total(), "two tote bags cost ₹1,798.00").toBe("Total: ₹1,798.00");
  });

  it("adds ₹30 for gift wrap", async () => {
    const { user, gift, total } = setup();
    await user.click(gift);
    expect(gift.checked, "the gift wrap box should be ticked after a click").toBe(true);
    expect(total(), "one amigurumi with gift wrap costs ₹480.00").toBe("Total: ₹480.00");
  });

  it("sends the whole request, with the quantity as a number", async () => {
    const { user, onSend, name, item, qty, gift, send } = setup();
    await user.type(name, "Asha Rao");
    await user.selectOptions(item, "KEY");
    await user.selectOptions(qty, "3");
    await user.click(gift);
    await user.click(send);
    expect(onSend.mock.calls.length, "pressing Send request should call onSend once").toBe(1);
    const request = onSend.mock.calls[0]?.[0];
    expect(typeof request?.qty, "qty should be the number 3, not the text \"3\"").toBe("number");
    expect(request, "onSend should get every field: name, item, qty and giftWrap").toEqual({
      name: "Asha Rao",
      item: "KEY",
      qty: 3,
      giftWrap: true,
    });
  });

  it("refuses to send without a name", async () => {
    const { user, onSend, name, send, error } = setup();
    await user.type(name, "   ");
    await user.click(send);
    expect(onSend.mock.calls.length, "a name of only spaces should not be sent").toBe(0);
    expect(error(), "the form should say Please enter your name").toBe("Please enter your name");
  });

  it("clears the error once the name is filled in and sent", async () => {
    const { user, onSend, name, send, error } = setup();
    await user.click(send);
    await user.type(name, "Asha Rao");
    await user.click(send);
    expect(onSend.mock.calls.length, "the second press, with a name, should send").toBe(1);
    expect(error(), "the error should be gone after a good send").toBe("");
  });
});
