import { Children, isValidElement } from "react";

/**
 * A root layout returns <html><body>...</body></html>, which can't be drawn inside a test's <div>.
 * This returns what the layout put inside <body>, so a test can render that instead.
 */
export function bodyContent(tree) {
  if (!isValidElement(tree) || tree.type !== "html") return [];
  const body = Children.toArray(tree.props.children).find((child) => isValidElement(child) && child.type === "body");
  return body ? Children.toArray(body.props.children) : [];
}
