import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { YarnPicker } from "../src/YarnPicker.tsx";

afterEach(cleanup);

function text(container: HTMLElement, selector: string) {
  return container.querySelector(selector)?.textContent;
}

describe("YarnPicker", () => {
  it("starts with no colour chosen and an empty bag", () => {
    const { container } = render(<YarnPicker />);
    expect(text(container, ".choice"), "before any tap, it should say Pick a colour").toBe("Pick a colour");
    expect(text(container, ".bag"), "the bag should start empty").toBe("Your bag is empty");
    const add = screen.queryByRole("button", { name: "Add to bag" }) as HTMLButtonElement | null;
    expect(add?.disabled, "Add to bag should be disabled until a colour is chosen").toBe(true);
  });

  it("remembers the colour you tap", async () => {
    const user = userEvent.setup();
    const { container } = render(<YarnPicker />);
    const mint = screen.queryByRole("button", { name: "Mint" });
    expect(mint, "there should be a button for Mint").not.toBeNull();
    await user.click(mint!);
    expect(text(container, ".choice"), "after tapping Mint, it should say Colour: Mint").toBe("Colour: Mint");
  });

  it("marks only the chosen colour as pressed", async () => {
    const user = userEvent.setup();
    render(<YarnPicker />);
    await user.click(screen.queryByRole("button", { name: "Indigo" })!);
    expect(screen.queryByRole("button", { name: "Indigo" })?.getAttribute("aria-pressed"), "Indigo should be pressed after tapping it").toBe("true");
    expect(screen.queryByRole("button", { name: "Mint" })?.getAttribute("aria-pressed"), "Mint should not be pressed").toBe("false");
  });

  it("puts the chosen colour in the bag", async () => {
    const user = userEvent.setup();
    const { container } = render(<YarnPicker />);
    await user.click(screen.queryByRole("button", { name: "Mint" })!);
    const add = screen.queryByRole("button", { name: "Add to bag" }) as HTMLButtonElement | null;
    expect(add?.disabled, "Add to bag should work once Mint is chosen").toBe(false);
    await user.click(add!);
    expect(text(container, ".bag"), "the bag should now hold Mint").toBe("In your bag: Mint");
  });

  it("adds each new colour to the end of the bag", async () => {
    const user = userEvent.setup();
    const { container } = render(<YarnPicker />);
    const add = screen.queryByRole("button", { name: "Add to bag" })!;
    await user.click(screen.queryByRole("button", { name: "Mint" })!);
    await user.click(add);
    await user.click(screen.queryByRole("button", { name: "Rani pink" })!);
    await user.click(add);
    expect(text(container, ".bag"), "Mint, then Rani pink, should both be in the bag, in that order").toBe("In your bag: Mint, Rani pink");
  });

  it("keeps a separate choice and bag for each picker", async () => {
    const user = userEvent.setup();
    const { container } = render(
      <>
        <YarnPicker />
        <YarnPicker />
      </>,
    );
    const mints = screen.queryAllByRole("button", { name: "Mint" });
    expect(mints.length, "each picker should have its own Mint button").toBe(2);
    await user.click(mints[0]);
    const choices = [...container.querySelectorAll(".choice")].map((p) => p.textContent);
    expect(choices.join(" and "), "tapping Mint in the first picker should not change the second").toBe("Colour: Mint and Pick a colour");
  });
});
