/**
 * The site address of an image from the catalogue, under the site's base path.
 * assetUrl("/shop/", "images/golu.png") is "/shop/images/golu.png".
 */
export function assetUrl(base, path) {
  const clean = path.replaceAll("\\", "/");
  if (/^[A-Za-z]:\//.test(clean) || /^\/(Users|home)\//.test(clean) || clean.startsWith("file:")) {
    throw new Error(`"${path}" is a path on one computer, not a path on the site`);
  }
  const relative = clean.replace(/^(\.\/|\/)+/, "");
  const prefix = base.endsWith("/") ? base : `${base}/`;
  return prefix + relative;
}
