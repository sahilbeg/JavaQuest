import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SearchBar } from "../src/SearchBar.jsx";

afterEach(cleanup);

describe("SearchBar", () => {
  it("puts the cursor in the box when you press Search", async () => {
    const user = userEvent.setup();
    render(<SearchBar />);
    await user.click(screen.getByRole("button", { name: "Search" }));
    expect(document.activeElement, "after pressing Search, the search input should have the focus").toBe(
      screen.getByLabelText("Search the shop"),
    );
  });

  it("lets you type straight after pressing Search", async () => {
    const user = userEvent.setup();
    render(<SearchBar />);
    await user.click(screen.getByRole("button", { name: "Search" }));
    await user.keyboard("golu");
    expect(screen.getByLabelText("Search the shop").value, "typing after Search should go into the box").toBe("golu");
  });

  it("empties the box and puts the cursor back when you press Clear", async () => {
    const user = userEvent.setup();
    render(<SearchBar />);
    const input = screen.getByLabelText("Search the shop");
    await user.type(input, "tote");
    await user.click(screen.getByRole("button", { name: "Clear" }));
    expect(input.value, "Clear should empty the box").toBe("");
    expect(document.activeElement, "after Clear, the search input should have the focus again").toBe(input);
  });
});
