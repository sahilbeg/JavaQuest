/** A function that handles one event's payload. */
export type Handler<P> = (payload: P) => void;

/** A typed event bus: each event name has its own payload type. */
export interface EventBus<Events> {
  on<K extends keyof Events>(type: K, handler: Handler<Events[K]>): () => void;
  once<K extends keyof Events>(type: K, handler: Handler<Events[K]>): () => void;
  emit<K extends keyof Events>(type: K, payload: Events[K]): void;
}

/** For each event name, the handlers listening to it. */
type Listeners<Events> = { [K in keyof Events]?: Handler<Events[K]>[] };

/**
 * Round 3. on(type, handler) subscribes and returns a function that unsubscribes exactly that subscription.
 * once(type, handler) does the same, for the next event only. emit(type, payload) calls every handler for type.
 */
export function createEventBus<Events>(): EventBus<Events> {
  const listeners: Listeners<Events> = {};

  function on<K extends keyof Events>(type: K, handler: Handler<Events[K]>): () => void {
    // A new function per subscription, so the same handler can subscribe twice and leave once.
    const entry: Handler<Events[K]> = (payload) => handler(payload);
    const before: Handler<Events[K]>[] = listeners[type] ?? [];
    // Replace the list instead of changing it, so an emit that is running keeps its own copy.
    listeners[type] = [...before, entry];
    return () => {
      const now: Handler<Events[K]>[] = listeners[type] ?? [];
      listeners[type] = now.filter((listener) => listener !== entry);
    };
  }

  function once<K extends keyof Events>(type: K, handler: Handler<Events[K]>): () => void {
    const off = on(type, (payload) => {
      off();
      handler(payload);
    });
    return off;
  }

  function emit<K extends keyof Events>(type: K, payload: Events[K]): void {
    const current: Handler<Events[K]>[] = listeners[type] ?? [];
    for (const handler of current) {
      handler(payload);
    }
  }

  return { on, once, emit };
}
