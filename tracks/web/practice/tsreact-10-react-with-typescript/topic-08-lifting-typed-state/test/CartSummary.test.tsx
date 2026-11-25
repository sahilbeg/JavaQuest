import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CartSummary } from "../src/CartSummary.tsx";

afterEach(cleanup);

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const bag = { id: "DC-1045", name: "Indigo tote bag", paise: 89900 };

describe("CartSummary", () => {
  it("says the cart is empty when there are no lines", () => {
    const { container } = render(<CartSummary lines={[]} onRemove={() => {}} />);
    expect(container.querySelector(".cart-empty")?.textContent, "no lines should show Your cart is empty").toBe("Your cart is empty");
  });

  it("tells the parent which item to remove", async () => {
    const user = userEvent.setup();
    const onRemove = vi.fn();
    render(
      <CartSummary
        lines={[
          { item: golu, qty: 1 },
          { item: bag, qty: 2 },
        ]}
        onRemove={onRemove}
      />,
    );
    const remove = screen.queryByRole("button", { name: "Remove Indigo tote bag" });
    expect(remove, "there should be a button labelled Remove Indigo tote bag").not.toBeNull();
    await user.click(remove!);
    expect(onRemove.mock.calls.length, "pressing Remove should call onRemove once").toBe(1);
    expect(onRemove.mock.calls[0]?.[0], "onRemove should get the tote bag's id").toBe("DC-1045");
  });
});
