import { describe, it, expect, afterEach, beforeEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { WishList } from "../src/WishList.tsx";

beforeEach(() => {
  localStorage.clear();
});

afterEach(cleanup);

function heart(name: string) {
  return screen.getByRole("button", { name: `Save ${name}` });
}

describe("WishList", () => {
  it("fills Golu's heart when you tap it", async () => {
    const user = userEvent.setup();
    render(<WishList />);
    await user.click(heart("Golu the elephant"));
    expect(heart("Golu the elephant").getAttribute("aria-pressed"), "after a tap, Golu's heart should be pressed").toBe("true");
  });

  it("keeps the heart after a refresh", async () => {
    const user = userEvent.setup();
    const { unmount } = render(<WishList />);
    await user.click(heart("Golu the elephant"));
    unmount();
    render(<WishList />);
    expect(heart("Golu the elephant").getAttribute("aria-pressed"), "Golu should still be saved after the list is mounted again").toBe("true");
    expect(heart("Indigo keychain").getAttribute("aria-pressed"), "the keychain was never saved").toBe("false");
  });

  it("shows and hides the saved ids", async () => {
    const user = userEvent.setup();
    render(<WishList />);
    await user.click(heart("Marigold coaster set"));
    await user.click(screen.getByRole("button", { name: "Show saved" }));
    expect(screen.queryByText("Saved: DC-2001"), "Show saved should list DC-2001").not.toBeNull();
    await user.click(screen.getByRole("button", { name: "Hide saved" }));
    expect(screen.queryByText("Saved: DC-2001"), "Hide saved should hide the list again").toBeNull();
  });

  it("starts empty when the saved wishlist is broken", async () => {
    const user = userEvent.setup();
    localStorage.setItem("dhaaga-wishlist", "{not json");
    render(<WishList />);
    await user.click(screen.getByRole("button", { name: "Show saved" }));
    expect(screen.queryByText("Saved: nothing yet"), "a broken saved list should fall back to an empty one").not.toBeNull();
  });
});
