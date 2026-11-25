import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import RootLayout, { metadata } from "../src/app/layout.jsx";
import { SiteShell } from "../src/components/SiteShell.jsx";

afterEach(cleanup);

describe("The root layout", () => {
  it("tells browsers and screen readers the page is in Indian English", () => {
    const page = RootLayout({ children: <p>Hello</p> });
    expect(page.type, "the root layout should return the <html> element").toBe("html");
    expect(page.props.lang, "the <html> element should have lang=\"en-IN\" (TODO 1)").toBe("en-IN");
  });

  it("gives every page its own title, ending with the shop's name", () => {
    expect(metadata.title, "metadata.title should be { default: \"Dhaaga & Co.\", template: \"%s | Dhaaga & Co.\" } (TODO 1)").toEqual({
      default: "Dhaaga & Co.",
      template: "%s | Dhaaga & Co.",
    });
  });

  it("describes the shop and knows its own address", () => {
    expect(typeof metadata.description === "string" && metadata.description.length > 0, "metadata should have a description for search results (TODO 1)").toBe(true);
    expect(String(metadata.metadataBase), "metadataBase should be new URL(\"https://dhaaga.in\") (TODO 1)").toBe("https://dhaaga.in/");
  });
});

describe("The site shell", () => {
  it("starts with a skip link to the main content", () => {
    render(
      <SiteShell>
        <h1>Golu the elephant</h1>
      </SiteShell>
    );
    const first = screen.getAllByRole("link")[0];
    expect(first.textContent, "the first link on every page should say Skip to content (TODO 2)").toBe("Skip to content");
    expect(first.getAttribute("href"), "the skip link should jump to #main (TODO 2)").toBe("#main");
  });

  it("puts the site links in a navigation landmark named Main", () => {
    render(
      <SiteShell>
        <h1>Golu the elephant</h1>
      </SiteShell>
    );
    const nav = screen.queryByRole("navigation", { name: "Main" });
    expect(nav, "the links should sit inside <nav aria-label=\"Main\"> (TODO 2)").not.toBeNull();
    expect(nav.textContent.includes("Cart"), "the Main navigation should hold the Cart link").toBe(true);
  });

  it("puts the page inside one main landmark with the id main", () => {
    render(
      <SiteShell>
        <h1>Golu the elephant</h1>
      </SiteShell>
    );
    const main = screen.queryByRole("main");
    expect(main, "the page should be inside a <main> element (TODO 2)").not.toBeNull();
    expect(main.id, "the <main> element needs id=\"main\", so the skip link can jump to it (TODO 2)").toBe("main");
    expect(main.textContent.includes("Golu the elephant"), "the page's own content should be inside <main>").toBe(true);
  });
});
