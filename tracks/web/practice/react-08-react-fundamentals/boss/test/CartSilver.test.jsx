import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Cart } from "../src/Cart.jsx";

afterEach(cleanup);

function mrsRaosCart() {
  return [
    { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 },
    { id: "DC-1046", name: "Baby booties", paise: 35000, qty: 2 },
    { id: "DC-1044", name: "Mint keychain", paise: 12000, qty: 3 },
  ];
}

describe("INC-08 silver: quantities the customer picked", () => {
  it("keeps a picked quantity when a row above it is removed", async () => {
    const user = userEvent.setup();
    render(<Cart initialItems={mrsRaosCart()} />);
    await user.selectOptions(screen.getByLabelText("Quantity for Mint keychain"), "5");
    await user.click(screen.getByRole("button", { name: "Remove Golu the elephant" }));
    expect(
      screen.queryByLabelText("Quantity for Mint keychain")?.value,
      "she picked 5 keychains; removing Golu must not change that",
    ).toBe("5");
    expect(screen.queryByLabelText("Quantity for Baby booties")?.value, "the booties should still say 2").toBe("2");
  });

  it("keeps every picked quantity when a row in the middle is removed", async () => {
    const user = userEvent.setup();
    render(<Cart initialItems={mrsRaosCart()} />);
    await user.selectOptions(screen.getByLabelText("Quantity for Golu the elephant"), "4");
    await user.selectOptions(screen.getByLabelText("Quantity for Mint keychain"), "5");
    await user.click(screen.getByRole("button", { name: "Remove Baby booties" }));
    expect(screen.queryByLabelText("Quantity for Golu the elephant")?.value, "Golu should still say 4").toBe("4");
    expect(screen.queryByLabelText("Quantity for Mint keychain")?.value, "the keychains should still say 5").toBe("5");
  });
});
