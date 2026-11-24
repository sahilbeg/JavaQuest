/** A link to a page of the store: <Link to="/cart">Cart</Link>. */
export function Link({ to, children }) {
  return <a href={"#" + to}>{children}</a>;
}
