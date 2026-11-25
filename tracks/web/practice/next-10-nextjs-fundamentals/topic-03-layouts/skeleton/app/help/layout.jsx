/** The layout for every page under /help. */
export default function HelpLayout({ children }) {
  return (
    <div className="help">
      {/* TODO 4: an <h2> that says Help centre, above the page */}
      {children}
      {/* TODO 5: a <p> that says Still stuck? WhatsApp us on 98765 43210. below the page */}
    </div>
  );
}
