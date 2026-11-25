import { describe, it, expect, afterEach } from "vitest";
import { value } from "./css.js";
import { SaleBadge } from "../src/sale-badge.js";

afterEach(() => {
  document.body.innerHTML = "";
});

function badge() {
  const element = document.createElement("sale-badge");
  document.body.append(element);
  return element;
}

/** The text of the badge's own shadow <style>. */
const shadowCss = (element) => element.shadowRoot?.querySelector("style")?.textContent ?? "";

describe("<sale-badge>", () => {
  it("is a SaleBadge with an open shadow root", () => {
    const element = badge();
    expect(element instanceof SaleBadge, "a <sale-badge> should be a SaleBadge").toBe(true);
    expect(element.shadowRoot?.mode, "the badge should have an open shadow root").toBe("open");
  });

  it("takes its colours from custom properties, with brand fallbacks", () => {
    const text = shadowCss(badge());
    expect(value(":host", "background", { text }), ":host's background should be var(--sale-badge-bg, #F2A81D)").toBe("var(--sale-badge-bg, #f2a81d)");
    expect(value(":host", "color", { text }), ":host's color should be var(--sale-badge-text, #2E3A8C)").toBe("var(--sale-badge-text, #2e3a8c)");
  });

  it("can still be hidden with the hidden attribute", () => {
    const text = shadowCss(badge());
    expect(value(":host([hidden])", "display", { text }), "the shadow style needs :host([hidden]) { display: none; }").toBe("none");
  });

  it("exposes its label as a part", () => {
    const label = badge().shadowRoot?.querySelector("[part=\"label\"]");
    expect(label?.tagName, "the shadow root should hold a <span part=\"label\">").toBe("SPAN");
    expect(label?.querySelector("slot")?.textContent, "the label should still hold the slot with Diwali offer as its fallback").toBe("Diwali offer");
  });
});
