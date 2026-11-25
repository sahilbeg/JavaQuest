import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { StockBadge } from "../src/StockBadge.tsx";

afterEach(cleanup);

function badge(stock: unknown) {
  const { container } = render(<StockBadge stock={stock as never} />);
  const p = container.querySelector("p.badge");
  return { text: p?.textContent, className: p?.className };
}

describe("StockBadge", () => {
  it("says In stock when plenty are left", () => {
    const shown = badge({ kind: "in-stock", count: 12 });
    expect(shown.text, "12 coaster sets left should say In stock").toBe("In stock");
    expect(shown.className, "plenty in stock should use badge badge--ok").toBe("badge badge--ok");
  });

  it("warns when only a few are left", () => {
    const shown = badge({ kind: "in-stock", count: 2 });
    expect(shown.text, "2 left should say Only 2 left").toBe("Only 2 left");
    expect(shown.className, "a low count should use badge badge--low").toBe("badge badge--low");
  });

  it("counts 3 as only a few", () => {
    expect(badge({ kind: "in-stock", count: 3 }).text, "3 left should say Only 3 left").toBe("Only 3 left");
    expect(badge({ kind: "in-stock", count: 4 }).text, "4 left is plenty, so it should say In stock").toBe("In stock");
  });

  it("says Sold out, with no date when none is known", () => {
    const shown = badge({ kind: "sold-out", backOn: null });
    expect(shown.text, "sold out with no date should say just Sold out").toBe("Sold out");
    expect(shown.className, "sold out should use badge badge--out").toBe("badge badge--out");
  });

  it("says when a sold-out item is back", () => {
    expect(badge({ kind: "sold-out", backOn: "14 Dec" }).text, "with a date, it should say Sold out, back on 14 Dec").toBe(
      "Sold out, back on 14 Dec",
    );
  });

  it("says how long a made-to-order item takes", () => {
    const shown = badge({ kind: "made-to-order", days: 5 });
    expect(shown.text, "made to order in 5 days should say Made to order: ready in 5 days").toBe("Made to order: ready in 5 days");
    expect(shown.className, "made to order should use badge badge--made").toBe("badge badge--made");
  });
});
