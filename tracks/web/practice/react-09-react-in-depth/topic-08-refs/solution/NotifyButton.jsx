import { useEffect, useRef, useState } from "react";

/** For a sold-out item: shows a message for 3 seconds after each tap. */
export function NotifyButton() {
  const [shown, setShown] = useState(false);
  const timerRef = useRef(null);

  useEffect(() => () => clearTimeout(timerRef.current), []);

  function handleClick() {
    clearTimeout(timerRef.current);
    setShown(true);
    timerRef.current = setTimeout(() => setShown(false), 3000);
  }

  return (
    <div>
      <button onClick={handleClick}>Notify me</button>
      {shown && <p role="status">We'll message you on WhatsApp when it's back.</p>}
    </div>
  );
}
