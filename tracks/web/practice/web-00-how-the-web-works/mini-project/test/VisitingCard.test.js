import { describe, it, expect } from "vitest";
import { page } from "./html.js";

const WHATSAPP = "https://wa.me/919876543210";

/** The text of the whole visible page, in lowercase. */
function bodyText() {
  return page().body.textContent.toLowerCase();
}

/** The <img> whose src is upi-qr.svg, or undefined. */
function qrImage() {
  return [...page().querySelectorAll("img")].find(
    (img) => (img.getAttribute("src") ?? "").trim().replace(/^\.\//, "") === "upi-qr.svg",
  );
}

/** The first <a> that links to Dhaaga's WhatsApp number, or undefined. */
function whatsappLink() {
  return [...page().querySelectorAll("a")].find((a) => (a.getAttribute("href") ?? "").trim().startsWith(WHATSAPP));
}

describe("The visiting card", () => {
  it("has one main heading that says Dhaaga & Co.", () => {
    const headings = page().querySelectorAll("h1");
    expect(headings.length, "the page should have exactly one <h1>").toBe(1);
    expect(headings[0].textContent.trim(), "the <h1> should say exactly Dhaaga & Co.").toBe("Dhaaga & Co.");
  });

  it("says in a paragraph that Dhaaga makes crochet", () => {
    const paragraphs = [...page().querySelectorAll("p")].map((p) => p.textContent.toLowerCase());
    expect(paragraphs.some((text) => text.includes("crochet")), "one <p> should use the word crochet").toBe(true);
  });

  it("says where to find the stall", () => {
    expect(bodyText().includes("fc road"), "the page should mention the FC Road stall").toBe(true);
  });

  it("says when the stall is open", () => {
    const text = bodyText();
    expect(text.includes("saturday") && text.includes("sunday"), "the page should say the stall opens Saturday and Sunday").toBe(true);
    expect(text.includes("10:00") && text.includes("20:00"), "the page should give the hours, 10:00 to 20:00").toBe(true);
  });

  it("shows the UPI QR code as an image", () => {
    expect(qrImage(), 'add <img src="upi-qr.svg" alt="..."> to the page').toBeDefined();
  });

  it("describes the QR code in alt text that mentions UPI", () => {
    const img = qrImage();
    expect(img, 'add <img src="upi-qr.svg" alt="..."> to the page').toBeDefined();
    const alt = (img.getAttribute("alt") ?? "").trim();
    expect(alt.length > 0, "the QR image needs alt text, for people who can't see it").toBe(true);
    expect(alt.toLowerCase().includes("upi"), "the alt text should say it's a UPI QR code").toBe(true);
  });

  it("links to Dhaaga's number on WhatsApp", () => {
    expect(whatsappLink(), `add a link whose href is ${WHATSAPP}`).toBeDefined();
  });

  it("gives the WhatsApp link words people can tap", () => {
    const link = whatsappLink();
    expect(link, `add a link whose href is ${WHATSAPP}`).toBeDefined();
    expect(link.textContent.toLowerCase().includes("whatsapp"), "the link text should mention WhatsApp, like Message us on WhatsApp").toBe(true);
  });
});
