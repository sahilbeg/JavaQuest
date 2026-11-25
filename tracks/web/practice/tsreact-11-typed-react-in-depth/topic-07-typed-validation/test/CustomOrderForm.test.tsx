import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CustomOrderForm } from "../src/CustomOrderForm.tsx";

afterEach(cleanup);

function send() {
  return screen.getByRole("button", { name: "Send request" });
}

describe("CustomOrderForm", () => {
  it("shows no messages before the first try", async () => {
    const user = userEvent.setup();
    render(<CustomOrderForm onSubmit={() => {}} />);
    await user.type(screen.getByLabelText("Phone"), "9876");
    expect(screen.queryAllByRole("alert").length, "nothing red should show while the customer is still typing").toBe(0);
  });

  it("shows a message under each bad field after Send", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<CustomOrderForm onSubmit={onSubmit} />);
    await user.click(send());
    expect(screen.queryByText("Please enter your name."), "an empty name should show its message after Send").not.toBeNull();
    expect(screen.queryByText("Phone needs 10 digits, like 98765 43210."), "an empty phone should show its message after Send").not.toBeNull();
    expect(onSubmit.mock.calls.length, "a form with errors must not be sent").toBe(0);
  });

  it("hides a message as soon as the field is fixed", async () => {
    const user = userEvent.setup();
    render(<CustomOrderForm onSubmit={() => {}} />);
    await user.click(send());
    await user.type(screen.getByLabelText("Name"), "Asha");
    expect(screen.queryByText("Please enter your name."), "typing a name should remove its message straight away").toBeNull();
    expect(screen.queryByText("Phone needs 10 digits, like 98765 43210."), "the phone message stays until the phone is fixed").not.toBeNull();
  });

  it("sends a clean, typed request when everything is right", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<CustomOrderForm onSubmit={onSubmit} />);
    await user.type(screen.getByLabelText("Name"), " Asha Rao ");
    await user.type(screen.getByLabelText("Phone"), "98765 43210");
    await user.selectOptions(screen.getByLabelText("Colour"), "Rani pink");
    await user.clear(screen.getByLabelText("How many"));
    await user.type(screen.getByLabelText("How many"), "3");
    await user.type(screen.getByLabelText("Name to stitch on (optional)"), "Golu");
    await user.click(send());
    expect(onSubmit.mock.calls.length, "a good form should call onSubmit once").toBe(1);
    expect(onSubmit.mock.calls[0][0], "onSubmit should get the clean request").toEqual({
      name: "Asha Rao",
      phone: "9876543210",
      colour: "Rani pink",
      qty: 3,
      stitchedName: "Golu",
    });
  });

  it("sends null when there is no name to stitch on", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<CustomOrderForm onSubmit={onSubmit} />);
    await user.type(screen.getByLabelText("Name"), "Farah");
    await user.type(screen.getByLabelText("Phone"), "9876543210");
    await user.click(send());
    expect(onSubmit.mock.calls[0]?.[0]?.stitchedName, "an empty stitched name should arrive as null").toBeNull();
    expect(onSubmit.mock.calls[0]?.[0]?.qty, "the starting quantity, 1, should arrive as the number 1").toBe(1);
  });
});
