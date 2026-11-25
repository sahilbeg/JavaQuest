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
    // TODO 4: the candidate's on and emit. Unsubscribing twice removes someone else's handler, and a handler
    // that unsubscribes while emit is running makes the next handler miss the event. Make each unsubscribe
    // remove exactly its own subscription, and let emit work on a list that can't change under it.
    const list: Handler<Events[K]>[] = listeners[type] ?? [];
    list.push(handler);
    listeners[type] = list;
    return () => {
      list.splice(list.indexOf(handler), 1);
    };
  }

  function once<K extends keyof Events>(type: K, handler: Handler<Events[K]>): () => void {
    // TODO 5: subscribe with on, so the handler runs for the next event only, then unsubscribes itself.
    // Return the unsubscribe function, so a once can also be cancelled before it fires.
    return () => {};
  }

  function emit<K extends keyof Events>(type: K, payload: Events[K]): void {
    const list: Handler<Events[K]>[] = listeners[type] ?? [];
    for (let i = 0; i < list.length; i++) {
      list[i](payload);
    }
  }

  return { on, once, emit };
}
