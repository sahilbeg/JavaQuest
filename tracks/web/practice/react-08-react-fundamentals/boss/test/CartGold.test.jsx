import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Cart } from "../src/Cart.jsx";

afterEach(cleanup);

// The Bengaluru customer: two different coaster sets that share a name.
function twoCoasterSets() {
  return [
    { id: "DC-1043", name: "Coaster set", paise: 15000, qty: 1 },
    { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 },
    { id: "DC-1047", name: "Coaster set", paise: 15000, qty: 4 },
  ];
}

describe("INC-08 gold: items with the same name", () => {
  it("removes only the coaster set that was clicked", async () => {
    const user = userEvent.setup();
    const errors = vi.spyOn(console, "error").mockImplementation(() => {});
    try {
      const { container } = render(<Cart initialItems={twoCoasterSets()} />);
      const removeButtons = screen.queryAllByRole("button", { name: "Remove Coaster set" });
      expect(removeButtons.length, "there should be a Remove button for each coaster set").toBe(2);
      await user.click(removeButtons[0]);

      expect(container.querySelectorAll("li.cart-row").length, "removing one coaster set should leave two rows").toBe(2);
      const coasterQty = screen.queryAllByLabelText("Quantity for Coaster set").map((select) => select.value);
      expect(coasterQty.join(", "), "the coaster set that is left should be the second one, with 4").toBe("4");
      expect(screen.queryByLabelText("Quantity for Golu the elephant")?.value, "Golu should still say 1").toBe("1");

      const keyWarnings = errors.mock.calls.filter((args) => args.map(String).join(" ").includes("key"));
      expect(keyWarnings.length, "React warned about keys: every row needs a key no other row has").toBe(0);
    } finally {
      errors.mockRestore();
    }
  });
});
