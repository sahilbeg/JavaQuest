/** A link to a page of the store: <Link to="/cart">Cart</Link>. */
export function Link({ to, children }) {
  // TODO 6: the href should be "#" followed by to, so "/cart" becomes "#/cart".
  return <a href="#">{children}</a>;
}
