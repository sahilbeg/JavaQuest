import { describe, it, expect } from "vitest";
import { metadata } from "../src/app/layout.jsx";

describe("The root layout's metadata", () => {
  it("sets the site's address as metadataBase", () => {
    expect(metadata.metadataBase instanceof URL, "metadataBase should be new URL(\"https://dhaaga.in\")").toBe(true);
    expect(metadata.metadataBase?.href, "metadataBase should be https://dhaaga.in").toBe("https://dhaaga.in/");
  });

  it("uses Dhaaga & Co. as the title of pages that set none", () => {
    expect(metadata.title?.default, "title.default should be Dhaaga & Co.").toBe("Dhaaga & Co.");
  });

  it("adds the shop's name to every page's own title", () => {
    expect(metadata.title?.template, "title.template should be \"%s | Dhaaga & Co.\"").toBe("%s | Dhaaga & Co.");
  });

  it("describes the shop", () => {
    expect(metadata.description, "description should be the sentence from README.md").toBe(
      "Handmade crochet from Pune: coasters, keychains, toys, bags and booties.",
    );
  });

  it("names the site on preview cards", () => {
    expect(metadata.openGraph, "metadata should have an openGraph object").toEqual({
      siteName: "Dhaaga & Co.",
      locale: "en_IN",
      type: "website",
    });
  });
});
