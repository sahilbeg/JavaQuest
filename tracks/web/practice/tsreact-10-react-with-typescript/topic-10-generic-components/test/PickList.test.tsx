import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PickList } from "../src/PickList.tsx";

afterEach(cleanup);

const BOXES = [
  { id: "box-none", label: "No box", paise: 0 },
  { id: "box-s", label: "Small box", paise: 3000 },
  { id: "box-l", label: "Large box", paise: 6000 },
];

function boxList(selected: (typeof BOXES)[number] | null, onPick = vi.fn()) {
  const view = render(
    <PickList
      label="Gift box"
      options={BOXES}
      selected={selected}
      getKey={(box) => box.id}
      renderOption={(box) => `${box.label} (+₹${box.paise / 100})`}
      onPick={onPick}
    />,
  );
  return { ...view, onPick };
}

function pressedTexts(container: HTMLElement) {
  return [...container.querySelectorAll("button")].filter((b) => b.getAttribute("aria-pressed") === "true").map((b) => b.textContent);
}

describe("PickList", () => {
  it("shows its label as the legend", () => {
    const { container } = boxList(null);
    expect(container.querySelector("fieldset.pick-list legend")?.textContent, "the legend should hold the label").toBe("Gift box");
  });

  it("draws one button per option, using renderOption for the text", () => {
    const { container } = boxList(null);
    const texts = [...container.querySelectorAll("fieldset button")].map((b) => b.textContent);
    expect(texts.join(" | "), "each box should be a button showing what renderOption returned").toBe(
      "No box (+₹0) | Small box (+₹30) | Large box (+₹60)",
    );
  });

  it("gives the very option object back when one is picked", async () => {
    const user = userEvent.setup();
    const { onPick } = boxList(null);
    const small = screen.queryByRole("button", { name: "Small box (+₹30)" });
    expect(small, "there should be a button for the small box").not.toBeNull();
    await user.click(small!);
    expect(onPick.mock.calls.length, "a tap should call onPick once").toBe(1);
    expect(onPick.mock.calls[0]?.[0], "onPick should get the small box object itself").toBe(BOXES[1]);
  });

  it("marks only the selected option as pressed, matching by key", () => {
    const copyOfLarge = { id: "box-l", label: "Large box", paise: 6000 };
    const { container } = boxList(copyOfLarge);
    expect(pressedTexts(container).join(" | "), "the large box should be pressed, even though selected is a copy").toBe("Large box (+₹60)");
    expect(container.querySelectorAll("button[aria-pressed='false']").length, "the other two should say aria-pressed false").toBe(2);
  });

  it("marks nothing as pressed when nothing is selected", () => {
    const { container } = boxList(null);
    expect(container.querySelectorAll("button").length, "all three buttons should be drawn").toBe(3);
    expect(pressedTexts(container).length, "with selected null, no button should be pressed").toBe(0);
  });

  it("works with plain strings too", async () => {
    const user = userEvent.setup();
    const onPick = vi.fn();
    const { container } = render(
      <PickList label="Yarn colour" options={["Marigold", "Mint"]} selected="Mint" getKey={(c) => c} renderOption={(c) => c} onPick={onPick} />,
    );
    expect(pressedTexts(container).join(","), "Mint should be pressed").toBe("Mint");
    await user.click(screen.queryByRole("button", { name: "Marigold" })!);
    expect(onPick.mock.calls[0]?.[0], "tapping Marigold should pick the string Marigold").toBe("Marigold");
  });

  it("gives every button a key, so React logs no key warnings", () => {
    const errors = vi.spyOn(console, "error").mockImplementation(() => {});
    try {
      const { container } = boxList(BOXES[0]);
      expect(container.querySelectorAll("button").length, "all three buttons should be drawn").toBe(3);
      const keyWarnings = errors.mock.calls.filter((args) => args.map(String).join(" ").includes("key"));
      expect(keyWarnings.length, "React warned about keys: use key={getKey(option)}").toBe(0);
    } finally {
      errors.mockRestore();
    }
  });
});
