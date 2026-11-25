import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { Panel } from "../src/Panel.tsx";

afterEach(cleanup);

describe("Panel", () => {
  it("shows its title in a heading", () => {
    const { container } = render(<Panel title="Delivery">Free above ₹999</Panel>);
    expect(container.querySelector("section.panel h2")?.textContent, "the <h2> should hold the title").toBe("Delivery");
  });

  it("shows text children inside the panel body", () => {
    const { container } = render(<Panel title="Delivery">Free above ₹999</Panel>);
    expect(container.querySelector(".panel-body")?.textContent, "the text written inside <Panel> should appear in .panel-body").toBe(
      "Free above ₹999",
    );
  });

  it("shows element children, like a list", () => {
    const { container } = render(
      <Panel title="Gift wrap">
        <ul>
          <li>Marigold paper</li>
          <li>Indigo ribbon</li>
        </ul>
      </Panel>,
    );
    const items = container.querySelectorAll(".panel-body li");
    expect(items.length, "both list items written inside <Panel> should appear in .panel-body").toBe(2);
  });

  it("keeps several children in the order they were written", () => {
    const { container } = render(
      <Panel title="Payment">
        <p>Cash</p>
        <p>UPI</p>
        <p>Card</p>
      </Panel>,
    );
    const texts = [...container.querySelectorAll(".panel-body p")].map((p) => p.textContent);
    expect(texts.join(", "), "the three paragraphs should appear in order").toBe("Cash, UPI, Card");
  });
});
