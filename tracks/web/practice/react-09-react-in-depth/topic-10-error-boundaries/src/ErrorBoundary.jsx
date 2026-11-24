import { Component } from "react";

/** Catches errors thrown while rendering its children, and shows a fallback instead. */
export class ErrorBoundary extends Component {
  state = { error: null };

  // TODO 1: add static getDerivedStateFromError(error), returning { error }.

  // TODO 2: add componentDidCatch(error), calling this.props.onError with the error if onError was given.

  render() {
    // TODO 3: if this.state.error is set, return <div role="alert"> with <p>{this.props.fallback ?? "Something went wrong."}</p>.
    // TODO 4: put a "Try again" button in that div that calls this.setState({ error: null }).
    return this.props.children;
  }
}
