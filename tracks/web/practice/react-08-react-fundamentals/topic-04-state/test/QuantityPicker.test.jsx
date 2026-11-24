import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QuantityPicker } from "../src/QuantityPicker.jsx";

afterEach(cleanup);

/** What the picker shows as the quantity, and as the line total. */
function shown(container) {
  return {
    qty: container.querySelector(".qty")?.textContent,
    total: container.querySelector(".line-total")?.textContent,
  };
}

function buttons() {
  return {
    plus: screen.queryByRole("button", { name: "One more" }),
    minus: screen.queryByRole("button", { name: "One less" }),
  };
}

describe("QuantityPicker", () => {
  it("starts at 1 and shows the price of one", () => {
    const { container } = render(<QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />);
    expect(shown(container).qty, "the picker should start at 1").toBe("1");
    expect(shown(container).total, "one coaster set costs ₹150.00").toBe("₹150.00");
  });

  it("counts up when you press +", async () => {
    const user = userEvent.setup();
    const { container } = render(<QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />);
    const { plus } = buttons();
    expect(plus, "there should be a button labelled One more").not.toBeNull();
    await user.click(plus);
    await user.click(plus);
    expect(shown(container).qty, "1 plus two presses of + should show 3").toBe("3");
  });

  it("shows the price of the whole quantity", async () => {
    const user = userEvent.setup();
    const { container } = render(<QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />);
    const { plus } = buttons();
    await user.click(plus);
    await user.click(plus);
    expect(shown(container).total, "three coaster sets cost ₹450.00").toBe("₹450.00");
  });

  it("counts down when you press -", async () => {
    const user = userEvent.setup();
    const { container } = render(<QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />);
    const { plus, minus } = buttons();
    await user.click(plus);
    await user.click(plus);
    await user.click(minus);
    expect(shown(container).qty, "+, +, then - should show 2").toBe("2");
    expect(shown(container).total, "two coaster sets cost ₹300.00").toBe("₹300.00");
  });

  it("disables - at 1, so the quantity never drops below 1", () => {
    render(<QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />);
    const { minus } = buttons();
    expect(minus, "there should be a button labelled One less").not.toBeNull();
    expect(minus.disabled, "the - button should be disabled while the quantity is 1").toBe(true);
  });

  it("enables - again once the quantity is above 1", async () => {
    const user = userEvent.setup();
    render(<QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />);
    const { plus, minus } = buttons();
    await user.click(plus);
    expect(minus.disabled, "at 2, the - button should work again").toBe(false);
  });

  it("disables + when the quantity reaches the stock", async () => {
    const user = userEvent.setup();
    const { container } = render(<QuantityPicker name="Golu the elephant" paise={45000} stock={3} />);
    const { plus } = buttons();
    expect(plus.disabled, "with 1 picked and 3 in stock, + should work").toBe(false);
    await user.click(plus);
    await user.click(plus);
    expect(shown(container).qty, "two presses should reach 3").toBe("3");
    expect(plus.disabled, "only 3 elephants are in stock, so + should be disabled at 3").toBe(true);
    expect(shown(container).total, "three elephants cost ₹1,350.00").toBe("₹1,350.00");
  });

  it("keeps a separate count for each picker", async () => {
    const user = userEvent.setup();
    const { container } = render(
      <>
        <QuantityPicker name="Golu the elephant" paise={45000} stock={3} />
        <QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />
      </>,
    );
    const plusButtons = screen.queryAllByRole("button", { name: "One more" });
    expect(plusButtons.length, "each picker should have its own + button").toBe(2);
    await user.click(plusButtons[0]);
    await user.click(plusButtons[0]);
    const counts = [...container.querySelectorAll(".qty")].map((span) => span.textContent);
    expect(counts.join(" and "), "pressing Golu's + should not change the coaster set's count").toBe("3 and 1");
  });
});
