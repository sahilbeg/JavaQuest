/** The layout for every page under /help. */
export default function HelpLayout({ children }) {
  return (
    <div className="help">
      <h2>Help centre</h2>
      {children}
      <p>Still stuck? WhatsApp us on 98765 43210.</p>
    </div>
  );
}
