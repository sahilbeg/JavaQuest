package jq.m04.linkedlists;

public class RiderList<T> {

    private static class Node<T> {
        T item;
        Node<T> next;

        Node(T item) {
            this.item = item;
        }
    }

    private Node<T> head;
    private int size;

    public void addFirst(T item) {
        // TODO 1: make a new node, point its next at head, make it the new head, add 1 to size
    }

    public void addLast(T item) {
        // TODO 2: if the list is empty, the new node becomes head; otherwise walk to the last node and link it there. Add 1 to size
    }

    public T removeFirst() {
        // TODO 3: return null when empty; otherwise keep head's item, move head to head.next, take 1 from size
        return null;
    }

    public T get(int index) {
        // TODO 4: throw IndexOutOfBoundsException when index < 0 or index >= size; otherwise step index times from head
        return null;
    }

    public int size() {
        // TODO 5: return the size counter
        return 0;
    }

    public boolean contains(T item) {
        // TODO 6: walk the nodes and return true when a node's item equals(item)
        return false;
    }

    public void reverse() {
        // TODO 7: walk with previous, current and next; point each node back at previous; head becomes the old last node
    }

    @Override
    public String toString() {
        // TODO 8: build "[a, b, c]" by walking the nodes; an empty list is "[]"
        return "";
    }

    public static void main(String[] args) {
        RiderList<String> waiting = new RiderList<>();
        waiting.addLast("Ana");
        waiting.addLast("Ben");
        waiting.addFirst("Cleo");
        System.out.println(waiting);
        System.out.println(waiting.removeFirst() + " got a bike");
        System.out.println(waiting);
    }
}
