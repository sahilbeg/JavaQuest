/** The top of every page: a skip link, the logo, the main menu and the cart count. */
export function Header({ cartCount }) {
  // TODO 9: make a "Skip to content" link to #main the very first thing in the header, so it is the first Tab stop.
  // Name the menu with aria-label="Main". Show the count as "Cart: 0 items", "Cart: 1 item", "Cart: 3 items" in a role="status" paragraph.
  return (
    <header className="site-header">
      <a href="/" className="logo">
        {"Dhaaga & Co."}
      </a>
      <nav>
        <ul>
          <li>
            <a href="#catalogue-title">Shop</a>
          </li>
          <li>
            <a href="#about">About us</a>
          </li>
          <li>
            <a href="#contact">Contact</a>
          </li>
        </ul>
      </nav>
      <p className="cart-count">Cart</p>
    </header>
  );
}
