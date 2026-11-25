import Link from "next/link";

// The frame around every page: header, navigation, the page itself, and the footer.
export function SiteShell({ children }) {
  // TODO 2: start with a link "Skip to content" to #main, wrap the links in <nav aria-label="Main">,
  // and put the page inside <main id="main"> instead of a div.
  return (
    <>
      <div className="top">
        <Link href="/">Dhaaga & Co.</Link>
        <div className="links">
          <Link href="/">Shop</Link>
          <Link href="/cart">Cart</Link>
        </div>
      </div>
      <div className="content">{children}</div>
      <footer>
        <p>Handmade in Pune</p>
      </footer>
    </>
  );
}
