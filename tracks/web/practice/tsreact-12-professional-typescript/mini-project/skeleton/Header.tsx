export interface HeaderProps {
  cartCount: number;
}

/** The top of every page: a skip link, the logo, the main menu and the cart count. */
export function Header({ cartCount }: HeaderProps) {
  // TODO 9: start the header with a "Skip to content" link to #main (class skip-link), so it is the first Tab stop.
  // Give the nav aria-label="Main". The count reads "Cart: 1 item" for one, and "Cart: 0 items" or "Cart: 3 items" otherwise.
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
      <p className="cart-count" role="status">
        Cart: {cartCount} items
      </p>
    </header>
  );
}
