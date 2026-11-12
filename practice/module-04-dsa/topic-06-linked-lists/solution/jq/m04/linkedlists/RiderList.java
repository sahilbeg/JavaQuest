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
        Node<T> node = new Node<>(item);
        node.next = head;
        head = node;
        size++;
    }

    public void addLast(T item) {
        Node<T> node = new Node<>(item);
        if (head == null) {
            head = node;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = node;
        }
        size++;
    }

    public T removeFirst() {
        if (head == null) {
            return null;
        }
        T item = head.item;
        head = head.next;
        size--;
        return item;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is outside a list of size " + size);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }

    public int size() {
        return size;
    }

    public boolean contains(T item) {
        Node<T> current = head;
        while (current != null) {
            if (current.item.equals(item)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public void reverse() {
        Node<T> previous = null;
        Node<T> current = head;
        while (current != null) {
            Node<T> next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }
        head = previous;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("[");
        Node<T> current = head;
        while (current != null) {
            text.append(current.item);
            if (current.next != null) {
                text.append(", ");
            }
            current = current.next;
        }
        return text.append("]").toString();
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
