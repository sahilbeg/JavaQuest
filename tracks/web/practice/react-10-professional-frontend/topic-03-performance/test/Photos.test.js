import { describe, it, expect } from "vitest";
import { photoUrl, photoSrcSet } from "../src/photos.js";

describe("photo addresses", () => {
  it("builds the address of one size of a photo", () => {
    expect(photoUrl("DC-1042", 400), "the 400-pixel Golu photo lives at /img/DC-1042-400.webp").toBe("/img/DC-1042-400.webp");
  });

  it("lists both sizes of a photo for srcset, with their widths", () => {
    expect(photoSrcSet("DC-1042"), "srcset should list the 400 and 800 pixel files, each followed by its width and a w").toBe(
      "/img/DC-1042-400.webp 400w, /img/DC-1042-800.webp 800w"
    );
    expect(photoSrcSet("DC-1301"), "the tote bag's srcset should use its own id").toBe("/img/DC-1301-400.webp 400w, /img/DC-1301-800.webp 800w");
  });
});
