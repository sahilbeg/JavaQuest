import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { HoursPanel } from "../src/HoursPanel.tsx";

afterEach(cleanup);

describe("HoursPanel", () => {
  it("is a Panel titled Opening hours", () => {
    const { container } = render(<HoursPanel />);
    expect(container.querySelector("section.panel h2")?.textContent, "HoursPanel should be a Panel with the title Opening hours").toBe(
      "Opening hours",
    );
  });

  it("lists the stall and the shop hours inside the panel body", () => {
    const { container } = render(<HoursPanel />);
    const lines = [...container.querySelectorAll(".panel-body p")].map((p) => p.textContent);
    expect(lines.length, "the panel body should hold two paragraphs").toBe(2);
    expect(lines[0], "the first line should give the stall's hours").toBe("Stall: Sat and Sun, 10:00 to 20:00");
    expect(lines[1], "the second line should give the Aundh shop's hours").toBe("Aundh shop: Tue to Sun, 11:00 to 21:00");
  });
});
