import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup, act, waitFor } from "@testing-library/react";
import { YarnPanel } from "../src/YarnPanel.tsx";
import { reply, stubFetch, heldFetch, settle } from "./api.ts";

const STOCK: Record<string, unknown> = {
  "/api/yarn/Marigold": { colour: "Marigold", grams: 1200 },
  "/api/yarn/Indigo": { colour: "Indigo", grams: 600 },
  "/api/yarn/Rani%20pink": { colour: "Rani pink", grams: 80 },
  "/api/yarn/Mint": { colour: "Mint", grams: "300" },
};

/** A pretend stock API: knows Marigold, Indigo and Rani pink, sends Mint with grams as text, and 404s the rest. */
function fakeApi() {
  return stubFetch(async (url) => (url in STOCK ? reply(STOCK[url]) : reply({ message: "not found" }, 404)));
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("YarnPanel", () => {
  it("says it is checking while it waits for the API", () => {
    stubFetch(() => new Promise(() => {}));
    render(<YarnPanel colour="Marigold" />);
    expect(screen.queryByText("Checking Marigold yarn..."), "before the answer arrives the panel should say Checking Marigold yarn...").not.toBeNull();
  });

  it("asks the API about the colour, once", async () => {
    const fetchMock = fakeApi();
    render(<YarnPanel colour="Marigold" />);
    await waitFor(() => expect(fetchMock.mock.calls.length, "the panel should call fetch once after it appears").toBe(1));
    expect(fetchMock.mock.calls[0][0], "the request should go to /api/yarn/Marigold").toBe("/api/yarn/Marigold");
  });

  it("shows the grams and how many amigurumi they make", async () => {
    fakeApi();
    render(<YarnPanel colour="Marigold" />);
    await waitFor(() =>
      expect(
        screen.queryByText("Marigold: 1200 g, enough for 30 amigurumi"),
        "1200 g at 40 g each should show Marigold: 1200 g, enough for 30 amigurumi",
      ).not.toBeNull(),
    );
  });

  it("puts a colour with a space safely into the address", async () => {
    const fetchMock = fakeApi();
    render(<YarnPanel colour="Rani pink" />);
    await waitFor(() => expect(fetchMock.mock.calls.length, "the panel should call fetch once").toBe(1));
    expect(fetchMock.mock.calls[0][0], "Rani pink should be asked for as /api/yarn/Rani%20pink (use encodeURIComponent)").toBe(
      "/api/yarn/Rani%20pink",
    );
    await waitFor(() =>
      expect(screen.queryByText("Rani pink: 80 g, enough for 2 amigurumi"), "80 g should make 2 amigurumi").not.toBeNull(),
    );
  });

  it("shows an error when the API answers 404", async () => {
    fakeApi();
    render(<YarnPanel colour="Off-white" />);
    await waitFor(() =>
      expect(screen.queryByRole("alert")?.textContent, "a 404 answer should show Couldn't load Off-white yarn. (check res.ok)").toBe(
        "Couldn't load Off-white yarn.",
      ),
    );
  });

  it("shows an error when the request fails completely", async () => {
    stubFetch(async () => {
      throw new TypeError("Failed to fetch");
    });
    render(<YarnPanel colour="Indigo" />);
    await waitFor(() =>
      expect(screen.queryByRole("alert")?.textContent, "when fetch throws, the panel should show Couldn't load Indigo yarn.").toBe(
        "Couldn't load Indigo yarn.",
      ),
    );
  });

  it("refuses an answer with the wrong shape", async () => {
    fakeApi();
    render(<YarnPanel colour="Mint" />);
    await waitFor(() =>
      expect(
        screen.queryByRole("alert")?.textContent,
        "Mint's answer has grams as text, so the panel should show Couldn't load Mint yarn. (check it with isYarnStock)",
      ).toBe("Couldn't load Mint yarn."),
    );
  });

  it("loads the new colour when colour changes", async () => {
    const fetchMock = fakeApi();
    const { rerender } = render(<YarnPanel colour="Marigold" />);
    await waitFor(() => expect(screen.queryByText("Marigold: 1200 g, enough for 30 amigurumi"), "Marigold should load first").not.toBeNull());
    rerender(<YarnPanel colour="Indigo" />);
    await waitFor(() =>
      expect(screen.queryByText("Indigo: 600 g, enough for 15 amigurumi"), "after switching to Indigo the panel should show its 600 g").not.toBeNull(),
    );
    expect(fetchMock.mock.calls.length, "one request for Marigold and one for Indigo").toBe(2);
  });

  it("says it is checking again while the new colour loads", async () => {
    const { waiting } = heldFetch();
    const { rerender } = render(<YarnPanel colour="Marigold" />);
    expect(typeof waiting["/api/yarn/Marigold"], "the panel should ask for Marigold").toBe("function");
    await act(async () => {
      waiting["/api/yarn/Marigold"](reply(STOCK["/api/yarn/Marigold"]));
      await settle();
    });
    rerender(<YarnPanel colour="Indigo" />);
    expect(screen.queryByText(/1200 g/), "Marigold's grams should not stay on the page under Indigo").toBeNull();
    expect(screen.queryByText("Checking Indigo yarn..."), "while Indigo loads the panel should say Checking Indigo yarn...").not.toBeNull();
  });

  it("ignores a slow answer for a colour it no longer shows", async () => {
    const { waiting } = heldFetch();
    const { rerender } = render(<YarnPanel colour="Marigold" />);
    rerender(<YarnPanel colour="Indigo" />);
    expect(typeof waiting["/api/yarn/Indigo"], "after switching, the panel should ask for Indigo").toBe("function");

    await act(async () => {
      waiting["/api/yarn/Indigo"](reply(STOCK["/api/yarn/Indigo"]));
      await settle();
    });
    expect(screen.queryByText("Indigo: 600 g, enough for 15 amigurumi"), "Indigo's answer should be shown").not.toBeNull();

    await act(async () => {
      waiting["/api/yarn/Marigold"](reply(STOCK["/api/yarn/Marigold"]));
      await settle();
    });
    expect(screen.queryByText(/1200 g/), "the late answer for Marigold should be ignored (use an ignore flag set in the cleanup)").toBeNull();
    expect(screen.queryByText("Indigo: 600 g, enough for 15 amigurumi"), "the panel should still show Indigo").not.toBeNull();
  });
});
