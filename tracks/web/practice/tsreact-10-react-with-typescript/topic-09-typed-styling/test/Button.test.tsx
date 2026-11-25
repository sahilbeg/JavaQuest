import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Button } from "../src/Button.tsx";

afterEach(cleanup);

function theButton(container: HTMLElement) {
  return container.querySelector("button") as HTMLButtonElement;
}

describe("Button", () => {
  it("shows its children", () => {
    const { container } = render(<Button>Pay by UPI</Button>);
    expect(theButton(container).textContent, "the button should show the text written inside it").toBe("Pay by UPI");
  });

  it("is a primary, medium, plain button by default", () => {
    const { container } = render(<Button>Pay by UPI</Button>);
    expect(theButton(container).className, "with no props, the classes should be btn btn--primary btn--md").toBe("btn btn--primary btn--md");
    expect(theButton(container).getAttribute("type"), "the default type should be button, so it never submits a form by accident").toBe("button");
  });

  it("uses the variant and size it is given", () => {
    const { container } = render(
      <Button variant="danger" size="sm">
        Remove
      </Button>,
    );
    expect(theButton(container).className, "variant danger and size sm should give btn btn--danger btn--sm").toBe("btn btn--danger btn--sm");
  });

  it("adds the parent's className after its own classes", () => {
    const { container } = render(
      <Button variant="secondary" className="wide">
        Keep shopping
      </Button>,
    );
    expect(theButton(container).className, "className=\"wide\" should be added at the end, not replace the others").toBe(
      "btn btn--secondary btn--md wide",
    );
  });

  it("passes other button props through", async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(
      <>
        <Button aria-label="Add Golu to cart" title="Golu" onClick={onClick}>
          Add
        </Button>
        <Button disabled>Sold out</Button>
      </>,
    );
    const add = screen.queryByRole("button", { name: "Add Golu to cart" });
    expect(add, "aria-label should reach the real button").not.toBeNull();
    expect(add?.getAttribute("title"), "title should reach the real button").toBe("Golu");
    await user.click(add!);
    expect(onClick.mock.calls.length, "onClick should run once when the button is clicked").toBe(1);
    const soldOut = screen.queryByRole("button", { name: "Sold out" }) as HTMLButtonElement | null;
    expect(soldOut?.disabled, "disabled should reach the real button").toBe(true);
  });

  it("can still be a submit button", () => {
    const { container } = render(<Button type="submit">Send request</Button>);
    expect(theButton(container).getAttribute("type"), "type=\"submit\" should replace the default").toBe("submit");
  });
});
