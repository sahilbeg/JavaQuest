import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { OrderForm } from "../src/OrderForm.jsx";

afterEach(cleanup);

function total(container) {
  return container.querySelector("p.total")?.textContent;
}

async function fillContact(user) {
  await user.type(screen.getByLabelText("Your name"), "Asha Deshpande");
  await user.type(screen.getByLabelText("Phone"), "98765 43210");
}

describe("OrderForm", () => {
  it("starts with one coaster set and a total of ₹150.00", () => {
    const { container } = render(<OrderForm onSubmit={vi.fn()} />);
    expect(screen.getByLabelText("Item").value, "the item should start as the coaster set, CST").toBe("CST");
    expect(screen.getByLabelText("Quantity").value, "the quantity should start at 1").toBe("1");
    expect(total(container), "one coaster set should show Total: ₹150.00").toBe("Total: ₹150.00");
  });

  it("keeps what the customer types in the name box", async () => {
    const user = userEvent.setup();
    render(<OrderForm onSubmit={vi.fn()} />);
    const name = screen.getByLabelText("Your name");
    await user.type(name, "Asha Deshpande");
    expect(name.value, "the name box should show what was typed").toBe("Asha Deshpande");
  });

  it("updates the total when another item and quantity are picked", async () => {
    const user = userEvent.setup();
    const { container } = render(<OrderForm onSubmit={vi.fn()} />);
    await user.selectOptions(screen.getByLabelText("Item"), "BAG");
    await user.selectOptions(screen.getByLabelText("Quantity"), "2");
    expect(screen.getByLabelText("Item").value, "the item select should now show the tote bag").toBe("BAG");
    expect(total(container), "two tote bags should show Total: ₹1,798.00").toBe("Total: ₹1,798.00");
  });

  it("adds ₹30 when gift wrap is ticked, and takes it off again", async () => {
    const user = userEvent.setup();
    const { container } = render(<OrderForm onSubmit={vi.fn()} />);
    await user.selectOptions(screen.getByLabelText("Item"), "AMI");
    const giftWrap = screen.getByLabelText("Gift wrap (₹30)");
    await user.click(giftWrap);
    expect(giftWrap.checked, "the gift wrap box should be ticked after one click").toBe(true);
    expect(total(container), "an amigurumi with gift wrap should show Total: ₹480.00").toBe("Total: ₹480.00");
    await user.click(giftWrap);
    expect(total(container), "unticking gift wrap should go back to Total: ₹450.00").toBe("Total: ₹450.00");
  });

  it("keeps Place order disabled until the name and phone are filled in", async () => {
    const user = userEvent.setup();
    render(<OrderForm onSubmit={vi.fn()} />);
    const button = screen.getByRole("button", { name: "Place order" });
    expect(button.disabled, "with an empty form, Place order should be disabled").toBe(true);
    await user.type(screen.getByLabelText("Your name"), "Asha Deshpande");
    expect(button.disabled, "with no phone number yet, Place order should still be disabled").toBe(true);
    await user.type(screen.getByLabelText("Phone"), "98765 43210");
    expect(button.disabled, "with a name and a phone, Place order should be enabled").toBe(false);
  });

  it("sends the whole order, with qty as a number", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<OrderForm onSubmit={onSubmit} />);
    await fillContact(user);
    await user.selectOptions(screen.getByLabelText("Item"), "AMI");
    await user.selectOptions(screen.getByLabelText("Quantity"), "3");
    await user.click(screen.getByLabelText("Gift wrap (₹30)"));
    await user.click(screen.getByRole("button", { name: "Place order" }));
    expect(onSubmit.mock.calls.length, "Place order should call onSubmit once").toBe(1);
    const order = onSubmit.mock.calls[0][0];
    expect(typeof order.qty, "qty should be a number, not text: use Number(...)").toBe("number");
    expect(order, "onSubmit should get { name, phone, code, qty, giftWrap }").toEqual({
      name: "Asha Deshpande",
      phone: "98765 43210",
      code: "AMI",
      qty: 3,
      giftWrap: true,
    });
  });

  it("stops the browser from reloading the page when the order is sent", async () => {
    const user = userEvent.setup();
    let prevented = null;
    const watch = (event) => {
      prevented = event.defaultPrevented;
    };
    document.addEventListener("submit", watch);
    try {
      render(<OrderForm onSubmit={vi.fn()} />);
      await fillContact(user);
      await user.click(screen.getByRole("button", { name: "Place order" }));
    } finally {
      document.removeEventListener("submit", watch);
    }
    expect(prevented, "the submit handler should call event.preventDefault()").toBe(true);
  });
});
