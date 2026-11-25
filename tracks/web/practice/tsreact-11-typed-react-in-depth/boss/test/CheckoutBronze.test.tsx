import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Checkout } from "../src/Checkout.tsx";
import { checkoutReducer, START } from "../src/checkout.ts";

afterEach(cleanup);

function heading() {
  return screen.queryByRole("heading", { level: 2 })?.textContent;
}

describe("Checkout (bronze)", () => {
  it("goes from the address to the gift step", () => {
    const next = checkoutReducer({ ...START, step: "address" }, { type: "next" });
    expect(next.step, "Continue on the address step should lead to the gift step").toBe("gift");
  });

  it("goes from the gift step on to UPI", () => {
    const next = checkoutReducer({ ...START, step: "gift" }, { type: "next" });
    expect(next.step, "Continue on the gift step should lead to upi").toBe("upi");
  });

  it("shows the gift step, with its own heading, on the way to payment", async () => {
    const user = userEvent.setup();
    render(<Checkout itemsPaise={45000} />);
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(heading(), "the second step should be Delivery address").toBe("Delivery address");
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(heading(), "after the address, the customer should see Make it a gift").toBe("Make it a gift");
    expect(screen.queryByLabelText("Gift wrap (₹30)"), "the gift step should offer gift wrap").not.toBeNull();
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(heading(), "after the gift step comes Pay by UPI").toBe("Pay by UPI");
  });
});
