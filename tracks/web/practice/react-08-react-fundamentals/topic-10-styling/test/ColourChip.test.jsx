import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ColourChip, PriceTag } from "../src/ColourChip.jsx";

afterEach(cleanup);

/** jsdom may give a colour back as rgb(...) or as the hex you wrote, so accept both. */
function sameColour(actual, hex, rgb) {
  const value = (actual ?? "").toLowerCase().replace(/\s+/g, " ");
  return value === hex.toLowerCase() || value === rgb;
}

describe("ColourChip", () => {
  it("has just the chip class when it is not picked", () => {
    render(<ColourChip colour="Mint" onSelect={vi.fn()} />);
    const chip = screen.getByRole("button", { name: "Mint" });
    expect(chip.className, "an unpicked chip should have className \"chip\"").toBe("chip");
  });

  it("adds chip--selected when it is picked", () => {
    render(<ColourChip colour="Mint" selected={true} onSelect={vi.fn()} />);
    const chip = screen.getByRole("button", { name: "Mint" });
    expect(chip.className, "a picked chip should have className \"chip chip--selected\"").toBe("chip chip--selected");
  });

  it("tells screen readers whether it is picked", () => {
    const { rerender } = render(<ColourChip colour="Mint" selected={true} onSelect={vi.fn()} />);
    const chip = screen.getByRole("button", { name: "Mint" });
    expect(chip.getAttribute("aria-pressed"), "a picked chip should have aria-pressed=\"true\"").toBe("true");
    rerender(<ColourChip colour="Mint" selected={false} onSelect={vi.fn()} />);
    expect(chip.getAttribute("aria-pressed"), "an unpicked chip should have aria-pressed=\"false\"").toBe("false");
  });

  it("paints the swatch in the chip's own colour", () => {
    const { container, rerender } = render(<ColourChip colour="Marigold" onSelect={vi.fn()} />);
    const swatch = container.querySelector("span.swatch");
    expect(swatch, "the chip should hold a <span className=\"swatch\">").not.toBeNull();
    expect(
      sameColour(swatch.style.backgroundColor, "#F2A81D", "rgb(242, 168, 29)"),
      `the Marigold swatch's backgroundColor should be HEX.Marigold, #F2A81D, but it was "${swatch.style.backgroundColor}"`,
    ).toBe(true);
    rerender(<ColourChip colour="Indigo" onSelect={vi.fn()} />);
    expect(
      sameColour(container.querySelector("span.swatch").style.backgroundColor, "#2E3A8C", "rgb(46, 58, 140)"),
      "the Indigo swatch's backgroundColor should be HEX.Indigo, #2E3A8C",
    ).toBe(true);
  });

  it("reports its colour when tapped", async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();
    render(<ColourChip colour="Rani pink" onSelect={onSelect} />);
    await user.click(screen.getByRole("button", { name: "Rani pink" }));
    expect(onSelect.mock.calls[0]?.[0], "tapping the chip should call onSelect(\"Rani pink\")").toBe("Rani pink");
  });
});

describe("PriceTag", () => {
  it("shows the price with the price class", () => {
    const { container } = render(<PriceTag paise={89900} />);
    const tag = container.querySelector("p");
    expect(tag.textContent, "89900 paise should show as ₹899.00").toBe("₹899.00");
    expect(tag.className, "a normal price should have className \"price\"").toBe("price");
  });

  it("adds price--sale for a sale price", () => {
    const { container } = render(<PriceTag paise={80900} onSale={true} />);
    expect(container.querySelector("p").className, "a sale price should have className \"price price--sale\"").toBe(
      "price price--sale",
    );
  });
});
