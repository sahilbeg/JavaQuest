import { describe, it, expect, beforeEach } from "vitest";
import { renderBadges } from "../src/badges.js";

let root;

beforeEach(() => {
  document.body.innerHTML = `
    <ul id="catalogue">
      <li>Golu the elephant <span class="badge" data-item="DC-1042"></span></li>
      <li>Marigold coaster set <span class="badge" data-item="DC-2001"></span></li>
      <li>Indigo tote bag <span class="badge" data-item="DC-3001"></span></li>
    </ul>
  `;
  root = document.querySelector("#catalogue");
});

const badge = (id) => document.querySelector(`[data-item="${id}"]`);

describe("renderBadges", () => {
  it("writes the right text on every badge", () => {
    renderBadges(root, { "DC-1042": 2, "DC-2001": 12, "DC-3001": 0 });
    expect(badge("DC-1042").textContent, "Golu has 2 left").toBe("Only 2 left");
    expect(badge("DC-2001").textContent, "the coaster set has 12").toBe("In stock");
    expect(badge("DC-3001").textContent, "the tote bag has none").toBe("Sold out");
  });

  it("sets data-level on every badge for Sana's colours", () => {
    renderBadges(root, { "DC-1042": 2, "DC-2001": 12, "DC-3001": 0 });
    expect(badge("DC-1042").dataset.level, "2 left is level \"low\"").toBe("low");
    expect(badge("DC-2001").dataset.level, "12 left is level \"ok\"").toBe("ok");
    expect(badge("DC-3001").dataset.level, "0 left is level \"out\"").toBe("out");
  });

  it("leaves a badge alone when its id isn't in the answer", () => {
    badge("DC-3001").textContent = "Only 1 left";
    renderBadges(root, { "DC-1042": 2 });
    expect(badge("DC-1042").textContent, "Golu's badge should be updated").toBe("Only 2 left");
    expect(badge("DC-3001").textContent, "the tote bag isn't in the answer, so its badge should keep its text").toBe("Only 1 left");
  });

  it("only touches badges inside root", () => {
    document.body.insertAdjacentHTML("beforeend", `<span id="cart-badge" data-item="DC-1042">in your cart</span>`);
    renderBadges(root, { "DC-1042": 2 });
    expect(document.querySelector("#cart-badge").textContent, "search inside root, not the whole document").toBe("in your cart");
  });
});
