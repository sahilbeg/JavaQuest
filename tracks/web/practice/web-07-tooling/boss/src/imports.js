/** Finished: the path an import points at. joinPath("src/components/Drawer.js", "../cart.js") is "src/cart.js". */
export function joinPath(fromFile, specifier) {
  const parts = fromFile.split("/").slice(0, -1);
  for (const part of specifier.split("/")) {
    if (part === "." || part === "") continue;
    if (part === "..") parts.pop();
    else parts.push(part);
  }
  return parts.join("/");
}

/**
 * The file an import in fromFile points at, from the list of files the deploy server has.
 * resolveImport("src/main.js", "./cart.js", ["src/main.js", "src/cart.js"]) is "src/cart.js".
 */
export function resolveImport(fromFile, specifier, files) {
  const wanted = joinPath(fromFile, specifier);

  // TODO 1: like the Linux server, accept only a file whose name equals wanted exactly, capitals included.
  //         When nothing matches, throw an Error starting "Cannot find". If a file matches with case ignored,
  //         add "did you mean <that file>?" to the message.
  const found = files.find((file) => file.toLowerCase() === wanted.toLowerCase());
  if (found === undefined) {
    throw new Error(`Cannot find "${specifier}" from ${fromFile}`);
  }
  return found;
}
