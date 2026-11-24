import { Component } from "react";

/** Catches errors thrown while rendering its children, and shows a fallback instead. */
export class ErrorBoundary extends Component {
  state = { error: null };

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error) {
    this.props.onError?.(error);
  }

  render() {
    if (this.state.error) {
      return (
        <div role="alert">
          <p>{this.props.fallback ?? "Something went wrong."}</p>
          <button onClick={() => this.setState({ error: null })}>Try again</button>
        </div>
      );
    }
    return this.props.children;
  }
}
