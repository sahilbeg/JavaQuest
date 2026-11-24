import { useEffect, useRef, useState } from "react";

/** For a sold-out item: shows a message for 3 seconds after each tap. */
export function NotifyButton() {
  const [shown, setShown] = useState(false);

  function handleClick() {
    // TODO 5: cancel the timer from an earlier tap, so the message gets a fresh 3 seconds.
    setShown(true);
    setTimeout(() => setShown(false), 3000);
    // TODO 6: keep this timer's id in a ref (useRef), and cancel it in an effect's cleanup when the button leaves the page.
  }

  return (
    <div>
      <button onClick={handleClick}>Notify me</button>
      {shown && <p role="status">We'll message you on WhatsApp when it's back.</p>}
    </div>
  );
}
