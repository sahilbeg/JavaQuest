import Link from "next/link";

// The frame around every page: header, navigation, the page itself, and the footer.
export function SiteShell({ children }) {
  return (
    <>
      <a href="#main" className="skip-link">
        Skip to content
      </a>
      <header>
        <Link href="/">Dhaaga & Co.</Link>
        <nav aria-label="Main">
          <ul>
            <li>
              <Link href="/">Shop</Link>
            </li>
            <li>
              <Link href="/cart">Cart</Link>
            </li>
          </ul>
        </nav>
      </header>
      <main id="main">{children}</main>
      <footer>
        <p>Handmade in Pune</p>
      </footer>
    </>
  );
}
