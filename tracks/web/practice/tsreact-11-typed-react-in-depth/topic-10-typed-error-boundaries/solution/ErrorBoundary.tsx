import { Component } from "react";
import type { ReactNode } from "react";

/** Turns anything that was thrown into a real Error, so .message can always be read. */
export function toError(value: unknown): Error {
  return value instanceof Error ? value : new Error(String(value));
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

  static getDerivedStateFromError(error: unknown): ErrorBoundaryState {
    return { error: toError(error) };
  }

  componentDidCatch(error: unknown) {
    this.props.onError?.(toError(error));
  }

  componentDidUpdate(prevProps: ErrorBoundaryProps) {
    if (this.state.error !== null && prevProps.resetKey !== this.props.resetKey) {
      this.setState({ error: null });
    }
  }

  /** Clears the error, so the children render again. */
  reset = () => {
    this.setState({ error: null });
  };

  render() {
    if (this.state.error !== null) return this.props.fallback(this.state.error, this.reset);
    return this.props.children;
  }
}
