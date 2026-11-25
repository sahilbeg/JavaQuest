import { Component } from "react";
import type { ReactNode } from "react";

/** Turns anything that was thrown into a real Error, so .message can always be read. */
export function toError(value: unknown): Error {
  // TODO 1: return value itself if it is an Error (instanceof); otherwise new Error(String(value)).
  return new Error("");
}

export interface ErrorBoundaryProps {
  children: ReactNode;
  /** What to show instead of the children: it gets the error and a function that tries again. */
  fallback: (error: Error, reset: () => void) => ReactNode;
  /** When this changes (like the item id), a caught error is cleared. */
  resetKey?: string;
  /** Told about every error the boundary catches. */
  onError?: (error: Error) => void;
}

interface ErrorBoundaryState {
  error: Error | null;
}

/** Catches errors thrown while rendering its children, and shows the fallback for this part only. */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { error: null };

  // TODO 2: static getDerivedStateFromError(error: unknown): ErrorBoundaryState, returning { error: toError(error) }.

  // TODO 3: componentDidCatch(error: unknown), calling this.props.onError with toError(error) if there is one.

  // TODO 4: componentDidUpdate(prevProps: ErrorBoundaryProps): if there is an error and
  //         prevProps.resetKey !== this.props.resetKey, clear it with this.setState({ error: null }).

  /** Clears the error, so the children render again. */
  reset = () => {
    this.setState({ error: null });
  };

  render() {
    // TODO 5: if this.state.error is not null, return this.props.fallback(this.state.error, this.reset).
    return this.props.children;
  }
}
