// h(tag, props, ...children): build one element from a short description.

/** Builds a new element. props may hold class, dataset, onClick-style listeners and plain attributes. */
export function h(tag, props, ...children) {
  const element = document.createElement(tag);
  for (const [name, value] of Object.entries(props ?? {})) {
    // TODO 1: skip a value that is null, undefined or false; the name "class" sets element.className
    // TODO 2: "dataset" is copied with Object.assign(element.dataset, value); a name like "onClick" with a function value adds a "click" listener
    // TODO 3: any other name is set with setAttribute; the value true becomes ""
  }
  // TODO 4: append each child of children.flat(Infinity): skip null, undefined and false; elements as they are, anything else as String(child)
  return element;
}
