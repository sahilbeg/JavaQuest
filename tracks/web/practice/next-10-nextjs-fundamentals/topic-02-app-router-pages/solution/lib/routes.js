/**
 * The address a file in app/ becomes, worked out the way Next.js's app router does, or null when it isn't a page.
 * routeFor("app/visit/page.jsx") is "/visit".
 */
export function routeFor(file) {
  const parts = file.split("/");
  const name = parts[parts.length - 1];
  if (name !== "page.js" && name !== "page.jsx") return null;
  const folders = parts.slice(1, -1);
  if (folders.some((folder) => folder.startsWith("_"))) return null;
  const kept = folders.filter((folder) => !(folder.startsWith("(") && folder.endsWith(")")));
  return "/" + kept.join("/");
}
