import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import RootLayout from "../src/app/layout.jsx";
import HelpLayout from "../src/app/help/layout.jsx";
import DeliveryPage from "../src/app/help/delivery/page.jsx";
import { bodyContent } from "./layout.js";

afterEach(cleanup);

const comesBefore = (a, b) => Boolean(a.compareDocumentPosition(b) & Node.DOCUMENT_POSITION_FOLLOWING);

describe("HelpLayout", () => {
  it("still shows the page it wraps", () => {
    render(
      <HelpLayout>
        <p>Returns page</p>
      </HelpLayout>,
    );
    expect(screen.queryByText("Returns page"), "HelpLayout should show the page ({children}) it wraps").not.toBeNull();
  });

  it("shows the Help centre heading above the page", () => {
    render(
      <HelpLayout>
        <p>Returns page</p>
      </HelpLayout>,
    );
    const heading = screen.queryByRole("heading", { level: 2, name: "Help centre" });
    expect(heading, "HelpLayout should have an <h2> that says Help centre").not.toBeNull();
    const page = screen.queryByText("Returns page");
    expect(page !== null && comesBefore(heading, page), "the Help centre heading should come before the page").toBe(true);
  });

  it("shows the WhatsApp number below the page", () => {
    render(
      <HelpLayout>
        <p>Returns page</p>
      </HelpLayout>,
    );
    const line = screen.queryByText("Still stuck? WhatsApp us on 98765 43210.");
    expect(line, "HelpLayout should have a <p> that says Still stuck? WhatsApp us on 98765 43210.").not.toBeNull();
    const page = screen.queryByText("Returns page");
    expect(page !== null && comesBefore(page, line), "the WhatsApp line should come after the page").toBe(true);
  });

  it("nests inside the root layout, around the Delivery page", () => {
    const { container } = render(
      <>
        {bodyContent(
          RootLayout({
            children: (
              <HelpLayout>
                <DeliveryPage />
              </HelpLayout>
            ),
          }),
        )}
      </>,
    );
    const main = container.querySelector("main");
    expect(main, "the root layout should have a <main> (TODO 2)").not.toBeNull();
    expect(main.querySelector("h2")?.textContent, "inside <main>, the help layout's <h2> should say Help centre").toBe("Help centre");
    expect(main.querySelector("h1")?.textContent, "inside the help layout, the Delivery page's <h1> should show").toBe("Delivery");
  });
});
