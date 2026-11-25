import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { PageLayout } from "../src/PageLayout.tsx";

afterEach(cleanup);

function renderLayout() {
  return render(
    <PageLayout header={<h1>Dhaaga & Co.</h1>}>
      <p className="hello">Welcome to the shop</p>
    </PageLayout>,
  );
}

describe("PageLayout", () => {
  it("puts the header slot inside header.page-header", () => {
    const { container } = renderLayout();
    expect(container.querySelector("header.page-header h1")?.textContent, "the header prop should appear inside <header className=\"page-header\">").toBe(
      "Dhaaga & Co.",
    );
  });

  it("puts the children inside main.page-main", () => {
    const { container } = renderLayout();
    expect(container.querySelector("main.page-main p.hello")?.textContent, "the children should appear inside <main className=\"page-main\">").toBe(
      "Welcome to the shop",
    );
  });

  it("shows the header before the main part", () => {
    const { container } = renderLayout();
    const parts = [...(container.querySelector(".page")?.children ?? [])].map((el) => el.tagName);
    expect(parts.join(" then "), "div.page should hold the header, then the main").toBe("HEADER then MAIN");
  });
});
