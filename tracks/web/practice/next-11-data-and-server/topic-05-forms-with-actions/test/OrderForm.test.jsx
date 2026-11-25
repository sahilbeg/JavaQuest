import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup, waitFor, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { OrderForm, START } from "../src/app/order/OrderForm.jsx";

afterEach(cleanup);

const PHONE_ERROR = {
  errors: { phone: "Phone needs 10 digits, like 98765 43210." },
  values: { name: "Asha Rao", phone: "98765 4321", itemId: "DC-4410", qty: "2" },
};

/** Types a name and a 9-digit phone, then presses Place order. */
async function fillAndSend(user) {
  await user.type(screen.getByLabelText("Name"), "Asha Rao");
  await user.type(screen.getByLabelText("Phone"), "98765 4321");
  await user.click(screen.getByRole("button", { name: "Place order" }));
}

describe("OrderForm", () => {
  it("sends the previous state and then the form's fields to the action", async () => {
    const user = userEvent.setup();
    const action = vi.fn(async () => PHONE_ERROR);
    render(<OrderForm action={action} />);
    await fillAndSend(user);
    await waitFor(() => expect(action.mock.calls.length, "pressing Place order should call the action once").toBe(1));
    const [prevState, formData] = action.mock.calls[0];
    expect(prevState, "the first argument should be the starting state: use useActionState(action, START)").toEqual(START);
    expect(formData?.get?.("phone"), "the second argument should be the form's FormData, holding the phone as typed").toBe("98765 4321");
  });

  it("shows the message the action sends back", async () => {
    const user = userEvent.setup();
    render(<OrderForm action={vi.fn(async () => PHONE_ERROR)} />);
    await fillAndSend(user);
    await waitFor(() =>
      expect(screen.queryByText("Phone needs 10 digits, like 98765 43210."), "the phone message from state.errors should appear").not.toBeNull(),
    );
    expect(screen.queryByRole("alert")?.textContent, "the message should be in a <p role=\"alert\">, so screen readers read it out").toBe(
      "Phone needs 10 digits, like 98765 43210.",
    );
  });

  it("keeps what the customer typed after a mistake", async () => {
    const user = userEvent.setup();
    render(<OrderForm action={vi.fn(async () => PHONE_ERROR)} />);
    await fillAndSend(user);
    await waitFor(() => expect(screen.queryByRole("alert"), "the error should show first").not.toBeNull());
    expect(screen.getByLabelText("Name").value, "the name should still be there (defaultValue={state.values.name})").toBe("Asha Rao");
    expect(screen.getByLabelText("Phone").value, "the phone should still be there (defaultValue={state.values.phone})").toBe("98765 4321");
  });

  it("disables the button and says it's busy while the order is placed", async () => {
    const user = userEvent.setup();
    let finish;
    const action = vi.fn(
      () =>
        new Promise((resolve) => {
          finish = resolve;
        }),
    );
    render(<OrderForm action={action} />);
    await fillAndSend(user);
    await waitFor(() =>
      expect(screen.queryByRole("button", { name: "Placing order..." }), "while the action runs, the button should say Placing order...").not.toBeNull(),
    );
    expect(screen.queryByRole("button", { name: "Placing order..." }).disabled, "the busy button should be disabled (disabled={isPending})").toBe(true);

    await act(async () => {
      finish(START);
    });
    await waitFor(() =>
      expect(screen.queryByRole("button", { name: "Place order" }), "when the action is done, the button should say Place order again").not.toBeNull(),
    );
  });
});
