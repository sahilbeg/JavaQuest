import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { OrderBoard } from "../src/OrderBoard.tsx";

afterEach(cleanup);

const TODAY = [
  { id: "order-17", customer: "Mrs. Kulkarni", status: "new" as const },
  { id: "order-18", customer: "Asha Rao", status: "packed" as const },
];

function line(text: string) {
  return screen.queryByText(text);
}

describe("OrderBoard", () => {
  it("lists every order with its status", () => {
    render(<OrderBoard start={TODAY} />);
    expect(line("order-17 for Mrs. Kulkarni: new"), "order-17 should show as new").not.toBeNull();
    expect(line("order-18 for Asha Rao: packed"), "order-18 should show as packed").not.toBeNull();
  });

  it("packs an order when you press Pack", async () => {
    const user = userEvent.setup();
    render(<OrderBoard start={TODAY} />);
    await user.click(screen.getByRole("button", { name: "Pack order-17" }));
    expect(line("order-17 for Mrs. Kulkarni: packed"), "after Pack, order-17 should be packed (use useReducer)").not.toBeNull();
    expect(screen.queryByRole("button", { name: "Ship order-17" }), "a packed order should now offer Ship").not.toBeNull();
  });

  it("ships, and then offers no Cancel", async () => {
    const user = userEvent.setup();
    render(<OrderBoard start={TODAY} />);
    await user.click(screen.getByRole("button", { name: "Ship order-18" }));
    expect(line("order-18 for Asha Rao: shipped"), "after Ship, order-18 should be shipped").not.toBeNull();
    expect(screen.queryByRole("button", { name: "Cancel order-18" }), "a shipped order can't be cancelled, so there is no Cancel button").toBeNull();
  });

  it("cancels an order when you press Cancel", async () => {
    const user = userEvent.setup();
    render(<OrderBoard start={TODAY} />);
    await user.click(screen.getByRole("button", { name: "Cancel order-17" }));
    expect(line("order-17 for Mrs. Kulkarni: cancelled"), "after Cancel, order-17 should be cancelled").not.toBeNull();
  });
});
