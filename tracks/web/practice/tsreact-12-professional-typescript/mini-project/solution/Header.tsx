export interface HeaderProps {
  cartCount: number;
}

/** The top of every page: a skip link, the logo, the main menu and the cart count. */
export function Header({ cartCount }: HeaderProps) {
  return (
    <header className="site-header">
      <a href="#main" className="skip-link">
        Skip to content
      </a>
      <a href="/" className="logo">
        {"Dhaaga & Co."}
      </a>
      <nav aria-label="Main">
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
        Cart: {cartCount} {cartCount === 1 ? "item" : "items"}
      </p>
    </header>
  );
}
