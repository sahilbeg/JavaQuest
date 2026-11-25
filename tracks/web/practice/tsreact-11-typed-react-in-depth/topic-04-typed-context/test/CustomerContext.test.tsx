import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { Component } from "react";
import type { ReactNode } from "react";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CustomerProvider, useCustomer } from "../src/CustomerContext.tsx";
import { Greeting } from "../src/Greeting.tsx";
import { AccountButton } from "../src/AccountButton.tsx";

beforeEach(() => {
  // React logs every error a boundary catches. Keep the test output quiet.
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

/** Catches an error thrown while rendering and shows its message, so the test can read it. */
class Catch extends Component<{ children: ReactNode }, { message: string | null }> {
  state = { message: null as string | null };
  static getDerivedStateFromError(error: Error) {
    return { message: error.message };
  }
  render() {
    return this.state.message === null ? this.props.children : <p role="alert">{this.state.message}</p>;
  }
}

/** The page's shape: the button and the greeting far apart, with no props between them. */
function Page() {
  return (
    <CustomerProvider>
      <header>
        <AccountButton />
      </header>
      <main>
        <section>
          <Greeting />
        </section>
      </main>
    </CustomerProvider>
  );
}

function greeting() {
  return screen.queryByText(/^Namaste/)?.textContent;
}

describe("Customer context", () => {
  it("greets a guest before anyone signs in", () => {
    render(<Page />);
    expect(greeting(), "with nobody signed in, the greeting should be Namaste! Sign in to see your orders.").toBe(
      "Namaste! Sign in to see your orders.",
    );
  });

  it("greets Asha by her first name after she signs in", async () => {
    const user = userEvent.setup();
    render(<Page />);
    await user.click(screen.getByRole("button", { name: "Sign in as Asha" }));
    expect(greeting(), "signing in from the header should reach the greeting: Namaste, Asha!").toBe("Namaste, Asha!");
  });

  it("goes back to the guest greeting after signing out", async () => {
    const user = userEvent.setup();
    render(<Page />);
    await user.click(screen.getByRole("button", { name: "Sign in as Asha" }));
    await user.click(screen.getByRole("button", { name: "Sign out" }));
    expect(greeting(), "after Sign out, the greeting should ask the customer to sign in again").toBe("Namaste! Sign in to see your orders.");
    expect(screen.queryByRole("button", { name: "Sign in as Asha" }), "the header should offer Sign in again").not.toBeNull();
  });

  it("gives the same customer to every reader", async () => {
    const user = userEvent.setup();
    function PhoneNote() {
      const { customer } = useCustomer();
      return <p>Phone: {customer ? customer.phone : "none"}</p>;
    }
    render(
      <CustomerProvider>
        <AccountButton />
        <PhoneNote />
      </CustomerProvider>,
    );
    await user.click(screen.getByRole("button", { name: "Sign in as Asha" }));
    expect(screen.queryByText("Phone: 9876543210"), "a second reader should see the same signed-in customer").not.toBeNull();
  });

  it("fails loudly when there is no provider", () => {
    render(
      <Catch>
        <Greeting />
      </Catch>,
    );
    expect(
      screen.queryByRole("alert")?.textContent,
      "useCustomer outside a provider should throw useCustomer must be used inside <CustomerProvider>",
    ).toBe("useCustomer must be used inside <CustomerProvider>");
  });
});
