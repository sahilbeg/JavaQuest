import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { ColourSwatch } from "../src/ColourSwatch.tsx";

afterEach(cleanup);

/** How the browser writes a colour once it is set, so #F2A81D and rgb(242, 168, 29) compare equal. */
function normalised(colour: string) {
  const probe = document.createElement("span");
  probe.style.backgroundColor = colour;
  return probe.style.backgroundColor;
}

function swatch(container: HTMLElement) {
  return container.querySelector("span.swatch") as HTMLElement;
}

describe("ColourSwatch", () => {
  it("is labelled with its colour's name", () => {
    const { container } = render(<ColourSwatch colour="Mint" />);
    expect(swatch(container).getAttribute("aria-label"), "screen readers should hear Mint").toBe("Mint");
  });

  it("is filled with Sana's exact Marigold", () => {
    const { container } = render(<ColourSwatch colour="Marigold" />);
    expect(swatch(container).style.backgroundColor, "the Marigold swatch should have background-color #F2A81D").toBe(normalised("#F2A81D"));
  });

  it("is filled with Sana's exact Indigo", () => {
    const { container } = render(<ColourSwatch colour="Indigo" />);
    expect(swatch(container).style.backgroundColor, "the Indigo swatch should have background-color #2E3A8C").toBe(normalised("#2E3A8C"));
  });

  it("is 24 pixels square by default", () => {
    const { container } = render(<ColourSwatch colour="Mint" />);
    expect(swatch(container).style.width, "the default width should be 24px").toBe("24px");
    expect(swatch(container).style.height, "the default height should be 24px").toBe("24px");
  });

  it("uses the size it is given", () => {
    const { container } = render(<ColourSwatch colour="Off-white" size={40} />);
    expect(swatch(container).style.width, "size={40} should make it 40px wide").toBe("40px");
    expect(swatch(container).style.height, "size={40} should make it 40px tall").toBe("40px");
  });
});
