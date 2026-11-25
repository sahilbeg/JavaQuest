/**
 * The address a file in app/ becomes, worked out the way Next.js's app router does, or null when it isn't a page.
 * routeFor("app/visit/page.jsx") is "/visit".
 */
export function routeFor(file) {
  const parts = file.split("/");
  // TODO 3: only files called page.js or page.jsx are pages. Return null for any other file.
  // TODO 4: the folders are the parts between "app" and the file name. If any folder starts with "_", return null.
  // TODO 5: leave out route groups like "(shop)", then return "/" followed by the other folders joined with "/".
  return null;
}
