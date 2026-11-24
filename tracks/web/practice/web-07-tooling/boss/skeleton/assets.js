/**
 * The site address of an image from the catalogue, under the site's base path.
 * assetUrl("/shop/", "images/golu.png") is "/shop/images/golu.png".
 */
export function assetUrl(base, path) {
  // TODO 4: turn every "\" into "/" with replaceAll.
  // TODO 5: throw an Error containing "one computer" for a drive letter (C:\ or D:/), /Users/, /home/ or file: path.
  // TODO 6: drop a leading "./" or "/" from the path, and make sure the base ends in exactly one "/".
  return base + path;
}
