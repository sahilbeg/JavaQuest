import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { YarnShelf } from "../src/YarnShelf.tsx";

afterEach(cleanup);

function shelf() {
  return [
    { id: "yarn-1", colour: "Marigold", grams: 120 },
    { id: "yarn-2", colour: "Mint", grams: 30 },
    { id: "yarn-3", colour: "Indigo", grams: 260 },
    { id: "yarn-4", colour: "Mint", grams: 75 },
    { id: "yarn-5", colour: "Rani pink", grams: 40 },
  ] as const;
}

function rows(container: HTMLElement) {
  return [...container.querySelectorAll(".yarn-list li")];
}

describe("YarnShelf", () => {
  it("draws one row per yarn, in shelf order", () => {
    const { container } = render(<YarnShelf yarns={shelf()} />);
    expect(rows(container).map((li) => li.textContent).join(" | "), "each cone should get a row like Marigold: 120 g, in shelf order").toBe(
      "Marigold: 120 g | Mint: 30 g | Indigo: 260 g | Mint: 75 g | Rani pink: 40 g",
    );
  });

  it("marks cones under 40 g as low, and no others", () => {
    const { container } = render(<YarnShelf yarns={shelf()} />);
    const low = rows(container).filter((li) => li.classList.contains("low")).map((li) => li.textContent);
    expect(low.join(" | "), "only the 30 g Mint cone is under 40 g (40 g exactly is fine)").toBe("Mint: 30 g");
    expect(rows(container)[0]?.getAttribute("class"), "a row that is not low should have no class at all").toBeNull();
  });

  it("puts the fewest grams first when asked", async () => {
    const user = userEvent.setup();
    const { container } = render(<YarnShelf yarns={shelf()} />);
    await user.click(screen.queryByRole("button", { name: "Fewest grams first" })!);
    expect(rows(container).map((li) => li.textContent).join(" | "), "rows should go from 30 g up to 260 g").toBe(
      "Mint: 30 g | Rani pink: 40 g | Mint: 75 g | Marigold: 120 g | Indigo: 260 g",
    );
  });

  it("goes back to shelf order", async () => {
    const user = userEvent.setup();
    const { container } = render(<YarnShelf yarns={shelf()} />);
    await user.click(screen.queryByRole("button", { name: "Fewest grams first" })!);
    await user.click(screen.queryByRole("button", { name: "Shelf order" })!);
    expect(rows(container)[0]?.textContent, "after Shelf order, Marigold should be first again").toBe("Marigold: 120 g");
    expect(rows(container).length, "all five cones should still be there").toBe(5);
  });

  it("sorts a copy and never changes the yarns it was given", async () => {
    const user = userEvent.setup();
    const yarns = [...shelf()];
    const { container } = render(<YarnShelf yarns={yarns} />);
    await user.click(screen.queryByRole("button", { name: "Fewest grams first" })!);
    expect(rows(container)[0]?.textContent, "the sorted list should start with the 30 g cone").toBe("Mint: 30 g");
    expect(yarns.map((y) => y.id).join(" "), "the array passed in must keep its shelf order").toBe("yarn-1 yarn-2 yarn-3 yarn-4 yarn-5");
  });

  it("adds up all the grams", () => {
    const { container } = render(<YarnShelf yarns={shelf()} />);
    expect(container.querySelector(".total")?.textContent, "120 + 30 + 260 + 75 + 40 is 525 g").toBe("Total: 525 g");
  });

  it("gives every row its own key, even with two Mint cones", async () => {
    const errors = vi.spyOn(console, "error").mockImplementation(() => {});
    try {
      const user = userEvent.setup();
      const { container } = render(<YarnShelf yarns={shelf()} />);
      await user.click(screen.queryByRole("button", { name: "Fewest grams first" })!);
      expect(rows(container).length, "all five rows should be drawn").toBe(5);
      const keyWarnings = errors.mock.calls.filter((args) => args.map(String).join(" ").includes("key"));
      expect(keyWarnings.length, "React warned about keys: key each row by yarn.id").toBe(0);
    } finally {
      errors.mockRestore();
    }
  });
});
