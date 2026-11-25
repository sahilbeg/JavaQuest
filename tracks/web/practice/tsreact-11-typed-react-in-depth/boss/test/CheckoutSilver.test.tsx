import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Checkout } from "../src/Checkout.tsx";
import { checkoutReducer, START } from "../src/checkout.ts";

afterEach(cleanup);

function heading() {
  return screen.queryByRole("heading", { level: 2 })?.textContent;
}

describe("Checkout (silver)", () => {
  it("goes back from UPI to the gift step, and from there to the address", () => {
    const fromUpi = checkoutReducer({ ...START, step: "upi" }, { type: "back" });
    expect(fromUpi.step, "Back on upi should return to the gift step").toBe("gift");
    const fromGift = checkoutReducer(fromUpi, { type: "back" });
    expect(fromGift.step, "Back on the gift step should return to the address").toBe("address");
  });

  it("keeps the gift choices when you go back and forth", () => {
    let state = checkoutReducer({ ...START, step: "gift" }, { type: "giftChosen", giftWrap: true, stitchedName: "Rohan" });
    state = checkoutReducer(state, { type: "next" });
    state = checkoutReducer(state, { type: "back" });
    expect(state.step, "Back from upi should land on the gift step").toBe("gift");
    expect(state.giftWrap, "gift wrap should still be chosen").toBe(true);
    expect(state.stitchedName, "the stitched name should still be Rohan").toBe("Rohan");
  });

  it("adds gift wrap and a stitched name to what the customer pays", async () => {
    const user = userEvent.setup();
    render(<Checkout itemsPaise={45000} />);
    await user.click(screen.getByRole("button", { name: "Continue" }));
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(heading(), "the third step should be the gift step").toBe("Make it a gift");
    await user.click(screen.getByLabelText("Gift wrap (₹30)"));
    await user.type(screen.getByLabelText("Name to stitch on (₹50)"), "Rohan");
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(
      screen.queryByText("Pay ₹530.00 to dhaaga@upi"),
      "Golu ₹450 + gift wrap ₹30 + a stitched name ₹50 should make Pay ₹530.00 to dhaaga@upi",
    ).not.toBeNull();
  });
});
