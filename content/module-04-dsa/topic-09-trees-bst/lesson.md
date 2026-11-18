## Hook

Picture the Harbor depot at closing time. Kakashi has a stack of 50,000 bike cards and wants B-1042. He could flip through every card. Or he could use a guessing game. He picks up a card in the middle: B-1500. Too big, so he only looks to the left. Next card: B-1000. Too small, so he only looks to the right. Each guess throws away half the pile.

Binary search on a sorted array already plays this game. The trouble is new bikes. Every new card must squeeze into the right spot, and shifting 50,000 cards is slow. In this lesson you build a structure that plays the guessing game *and* takes new bikes cheaply. It is called a binary search tree.

## Concept

### Trees

A **tree** is a set of boxes joined by arrows that point downward. Each box is a **node**. A node holds a value, such as a bike number.

- The **root** is the single node at the top.
- A node's **children** are the nodes its arrows point to.
- A node's **parent** is the node that points to it. Only the root has no parent.
- A **leaf** is a node with no children.
- A **subtree** is a node together with everything below it.

```mermaid
flowchart TD
    A["1042 (root)"] --> B[1017]
    A --> C[1088]
    B --> D["1003 (leaf)"]
    B --> E["1025 (leaf)"]
    C --> F["1061 (leaf)"]
    C --> G["1099 (leaf)"]
```

In a **binary tree**, each node has at most two children: a **left child** and a **right child**.

```callout analogy
Gojo says: a tree is a family tree drawn upside down. The root is the great-grandparent at the top, and the leaves are the youngest kids at the bottom.
```

### Syntax: a node class

A node is a small object with a value and two links. A link that points nowhere is `null`.

```java
class Node {
    int bike;
    Node left;
    Node right;

    Node(int bike) {
        this.bike = bike;
    }
}
```

The tree itself only keeps the root. Every other node is reached by following links from there.

```java nocompile
Node root = new Node(1042);
root.left = new Node(1017);
root.right = new Node(1088);
```

### The BST rule

A **binary search tree** (BST) is a binary tree with one extra rule. For every node:

- every value in its left subtree is **smaller**,
- every value in its right subtree is **larger**.

Look at the picture again. Everything left of 1042 is smaller than 1042. Everything right of it is larger. The same holds at 1017 and at 1088. This rule is what makes search fast. At each node you know which half to ignore.

### Syntax: recursive search

The search looks at one node and then calls itself on one child. A `null` node means you fell off the tree, so the bike is not there.

```java nocompile
static boolean contains(Node node, int bike) {
    if (node == null) {
        return false;
    }
    if (bike == node.bike) {
        return true;
    }
    if (bike < node.bike) {
        return contains(node.left, bike);
    }
    return contains(node.right, bike);
}
```

Searching for 1025 in the picture: 1025 is smaller than 1042, so go left. 1025 is larger than 1017, so go right. Found it in three steps.

The smallest bike uses the same idea without any comparisons. Keep going left until there is no left child.

```java nocompile
static int min(Node node) {
    while (node.left != null) {
        node = node.left;
    }
    return node.bike;
}
```

### Syntax: recursive insert

Insert walks the same path as search. Where the search would hit `null`, insert hangs a new node. The helper returns the node, so the parent can store the link.

```java nocompile
static Node insert(Node node, int bike) {
    if (node == null) {
        return new Node(bike);
    }
    if (bike < node.bike) {
        node.left = insert(node.left, bike);
    } else if (bike > node.bike) {
        node.right = insert(node.right, bike);
    }
    return node;
}
```

When `bike` equals `node.bike`, neither branch runs. The duplicate is ignored. You call it as `root = insert(root, 1042);` so that the first insert sets the root.

### Deleting from a BST

Bike B-1042 is sold, so it leaves the registry. Find its node the same way search does. Then there are three cases.

1. **No children (a leaf).** Just cut it off: the parent's link becomes `null`.
2. **One child.** Let the child take the node's place. The parent now links to the child.
3. **Two children.** Copy in the in-order successor. Then delete that successor from the right subtree. It never has a left child, so that delete is case 1 or 2.

```callout jargon
A node's **in-order successor** is the next value in sorted order: the smallest value in its right subtree. Keep going left from the right child to find it.
```

```java nocompile
static Node delete(Node node, int bike) {
    if (node == null) {
        return null;
    }
    if (bike < node.bike) {
        node.left = delete(node.left, bike);
    } else if (bike > node.bike) {
        node.right = delete(node.right, bike);
    } else if (node.left == null) {
        return node.right;
    } else if (node.right == null) {
        return node.left;
    } else {
        Node next = node.right;
        while (next.left != null) {
            next = next.left;
        }
        node.bike = next.bike;
        node.right = delete(node.right, next.bike);
    }
    return node;
}
```

The first two cases share one trick: returning the other child covers both "no children" and "one child". Delete 1042 from the picture above, and 1061 moves up to the root. Every value left of it is still smaller, and every value right of it is still larger.

### Traversals

A **traversal** visits every node once. There are three common orders. Each one is three lines; only their order changes.

| Name | Order | Use |
|---|---|---|
| in-order | left, node, right | lists a BST in sorted order |
| pre-order | node, left, right | copies a tree top down |
| post-order | left, right, node | deletes or sums a tree bottom up |

### Syntax: in-order traversal

```java nocompile
static void inOrder(Node node, List<Integer> out) {
    if (node == null) {
        return;
    }
    inOrder(node.left, out);
    out.add(node.bike);
    inOrder(node.right, out);
}
```

Why is it sorted? Everything on the left is smaller, so it is added first. Then the node. Then everything larger. For the picture above, in-order gives `[1003, 1017, 1025, 1042, 1061, 1088, 1099]`. Pre-order gives `[1042, 1017, 1003, 1025, 1088, 1061, 1099]`. Post-order gives `[1003, 1025, 1017, 1061, 1099, 1088, 1042]`.

### Level-order traversal

The three orders above all dive deep first. Level-order reads the tree the way you read a page.

```callout jargon
A **level-order traversal** visits the root, then every node one level down from left to right, and so on. It is also called **breadth-first search** (BFS) on a tree.
```

The tool is a queue. Put the root in. Then, until the queue is empty: take a node out, record it, and put its children in at the back. Children always wait behind the nodes above them, so levels come out in order.

```java nocompile
Queue<Node> queue = new ArrayDeque<>();
queue.add(root);
while (!queue.isEmpty()) {
    int levelSize = queue.size();
    List<Integer> level = new ArrayList<>();
    for (int i = 0; i < levelSize; i++) {
        Node node = queue.poll();
        level.add(node.bike);
        if (node.left != null) {
            queue.add(node.left);
        }
        if (node.right != null) {
            queue.add(node.right);
        }
    }
    System.out.println(level);
}
```

Reading `queue.size()` at the start of each round tells you how many nodes belong to this level. For the picture above, it prints `[1042]`, then `[1017, 1088]`, then `[1003, 1025, 1061, 1099]`. Use it for "the nearest leaf" or "print the tree by rows".

```animate
title: Level order with a queue
kind: scene
nodes:
  - { id: queue, label: Queue, emoji: "🚏", value: "[1042]" }
  - { id: out, label: Levels printed, emoji: "📋", value: none yet }
steps:
  - say: Start with only the root, 1042, in the queue.
    focus: [queue]
  - say: Level 1 has one node. Take out 1042 and add its children, 1017 and 1088, at the back.
    set: { queue: "[1017, 1088]", out: "[1042]" }
  - say: Level 2 has two nodes. Take out 1017, add 1003 and 1025. Take out 1088, add 1061 and 1099.
    set: { queue: "[1003, 1025, 1061, 1099]", out: "[1042] [1017, 1088]" }
  - say: Level 3 has four leaves. They have no children, so nothing new joins the queue.
    set: { queue: "[]", out: "[1042] [1017, 1088] [1003, 1025, 1061, 1099]" }
  - say: The queue is empty, so the walk is over. Every node came out one row at a time.
    focus: [out]
```

### Height

The **height** of a tree is the number of nodes on the longest path from the root down to a leaf. In this course an empty tree has height 0 and a tree with one node has height 1. The picture above has height 3.

```java nocompile
static int height(Node node) {
    if (node == null) {
        return 0;
    }
    return 1 + Math.max(height(node.left), height(node.right));
}
```

```callout jargon
Gojo says: some books count edges instead of nodes, so a single node has height 0 there. Both are fine. Just pick one and write it down.
```

### Pass values up

`height` shows a pattern that solves many tree problems. Each call works out a small answer about its own subtree and **returns it up** to its parent. The parent combines the answers from its two children.

A tree is **height-balanced** when, at every node, the left and right heights differ by at most 1. You can check this in one pass. Return the height, or `-1` as a signal that something below is already unbalanced.

```java nocompile
static int checkedHeight(Node node) {
    if (node == null) {
        return 0;
    }
    int left = checkedHeight(node.left);
    int right = checkedHeight(node.right);
    if (left == -1 || right == -1 || Math.abs(left - right) > 1) {
        return -1;
    }
    return 1 + Math.max(left, right);
}
```

The tree is balanced when `checkedHeight(root) != -1`. Each node is visited once, so it is O(n). Calling `height` again at every node would be O(n²).

The same pattern finds the **diameter**, the longest path between any two nodes. At each node, the longest path through it, counted in edges, is `left height + right height`. Keep the best one you see while the heights travel up.

### Validating a BST

Is a tree really a BST? Checking each node against its two children is not enough. Look at this tree: 1042 has left child 1017, and 1017 has right child 1050. Each parent and child pair looks fine. But 1050 sits in the left subtree of 1042, and 1050 is larger than 1042. The rule is broken.

The fix: pass **bounds** down. Every node must sit strictly between a lower and an upper limit. Going left, the node's value becomes the new upper limit. Going right, it becomes the new lower limit.

```java nocompile
static boolean isBst(Node node, long low, long high) {
    if (node == null) {
        return true;
    }
    if (node.bike <= low || node.bike >= high) {
        return false;
    }
    return isBst(node.left, low, node.bike) && isBst(node.right, node.bike, high);
}
```

Start with `isBst(root, Long.MIN_VALUE, Long.MAX_VALUE)`. The limits are `long`, so even a node holding `Integer.MAX_VALUE` fits inside them. For the broken tree, 1050 must be below 1042, so the check returns `false`.

### Lowest common ancestor

```callout jargon
The **lowest common ancestor** (LCA) of two nodes is the deepest node that has both of them in its subtree. A node counts as part of its own subtree.
```

In the picture, the LCA of 1003 and 1025 is 1017. The LCA of 1025 and 1061 is the root, 1042.

In a BST you can find it by walking down from the root. If both values are smaller than the node, go left. If both are larger, go right. Otherwise they split here, or one of them is this node, so this node is the answer.

```java nocompile
static Node lca(Node node, int a, int b) {
    while (node != null) {
        if (a < node.bike && b < node.bike) {
            node = node.left;
        } else if (a > node.bike && b > node.bike) {
            node = node.right;
        } else {
            return node;
        }
    }
    return null;
}
```

It walks one path, so it costs the height of the tree. A plain binary tree has no order rule, so you must search both sides. Return the node if it matches, and recurse left and right. The node where both sides found something is the LCA.

### Balanced and degenerate trees

Search and insert walk one path from the root. So their cost is the height of the tree.

A **balanced** tree keeps both sides about equally tall. Each level doubles the number of nodes, so 1,000,000 bikes fit in about 20 levels. That is O(log n).

Now feed bikes in sorted order: 1001, 1002, 1003, 1004. Each new bike is larger than all others, so it always goes right. The tree becomes one long chain. This is a **degenerate** tree. Its height equals the number of bikes, so search is O(n), no better than a list.

```mermaid
flowchart TD
    A[1001] --> B[1002]
    B --> C[1003]
    C --> D[1004]
```

| Operation | Balanced tree | Degenerate tree |
|---|---|---|
| search, insert | O(log n): double the bikes, add one step | O(n): double the bikes, double the time |
| min, max | O(log n) | O(n) |
| in-order listing | O(n): visits each bike once | O(n) |

### Syntax: TreeMap

Java ships a BST that rebalances itself after every change, so it never degenerates. It is called `TreeMap`. It works like a `HashMap`, but its keys always stay sorted.

```java nocompile
TreeMap<Integer, String> bikes = new TreeMap<>();
bikes.put(1042, "Harbor");
bikes.put(1003, "Uptown");
System.out.println(bikes.firstKey());
```

`TreeMap` gives O(log n) for `put`, `get` and `containsKey`. `firstKey()` and `lastKey()` give the smallest and largest key. You need `import java.util.TreeMap;`.

### TreeMap navigation

A `HashMap` can only answer "is this exact key here?". A `TreeMap` keeps its keys sorted, so it also answers "what is nearest?". Each of these costs O(log n).

| Method | Returns |
|---|---|
| `floorKey(k)` | the largest key less than or equal to `k`, or `null` |
| `ceilingKey(k)` | the smallest key greater than or equal to `k`, or `null` |
| `headMap(k)` | a view of every entry with a key less than `k` |
| `subMap(from, to)` | a view of the keys from `from` (included) up to `to` (left out) |
| `pollFirstEntry()` | removes and returns the entry with the smallest key |

```java run
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        TreeMap<Integer, String> bikes = new TreeMap<>();
        bikes.put(1003, "Uptown");
        bikes.put(1017, "Old Mill");
        bikes.put(1042, "Harbor");
        bikes.put(1088, "Riverside");
        bikes.put(1099, "Depot");
        System.out.println(bikes.floorKey(1050));
        System.out.println(bikes.ceilingKey(1050));
        System.out.println(bikes.ceilingKey(2000));
        System.out.println(bikes.headMap(1042));
        System.out.println(bikes.subMap(1017, 1090));
        System.out.println(bikes.pollFirstEntry());
        System.out.println(bikes.firstKey());
    }
}
```

It prints `1042`, `1088`, `null`, then `{1003=Uptown, 1017=Old Mill}` and `{1017=Old Mill, 1042=Harbor, 1088=Riverside}`. Finally `pollFirstEntry` removes `1003=Uptown`, so the first key becomes `1017`. `TreeSet` has the same methods without values: `floor`, `ceiling`, `headSet`, `subSet` and `pollFirst`.

## Worked example

This program builds a registry by hand and lists it in order.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    static Node root;

    static Node insert(Node node, int bike) {
        if (node == null) {
            return new Node(bike);
        }
        if (bike < node.bike) {
            node.left = insert(node.left, bike);
        } else if (bike > node.bike) {
            node.right = insert(node.right, bike);
        }
        return node;
    }

    static void inOrder(Node node, List<Integer> out) {
        if (node == null) {
            return;
        }
        inOrder(node.left, out);
        out.add(node.bike);
        inOrder(node.right, out);
    }

    public static void main(String[] args) {
        int[] arrivals = {1042, 1017, 1088, 1003, 1025};
        for (int bike : arrivals) {
            root = insert(root, bike);
        }
        List<Integer> sorted = new ArrayList<>();
        inOrder(root, sorted);
        System.out.println(sorted);
        System.out.println("Root: " + root.bike);
    }
}

class Node {
    int bike;
    Node left;
    Node right;

    Node(int bike) {
        this.bike = bike;
    }
}
```

Line by line:

- `static Node root;` starts as `null`, an empty tree.
- The first `insert` sees `null` and returns a new node. That node becomes the root, 1042.
- 1017 is smaller, so it goes left of 1042. 1088 goes right.
- 1003 goes left of 1042, then left of 1017. 1025 goes left of 1042, then right of 1017.
- `inOrder` visits left, node, right, so it prints `[1003, 1017, 1025, 1042, 1088]`.
- The root stays 1042, the first bike that arrived.

## Try it

This program inserts bikes in sorted order and measures the height. Run it, then change the array to `{1003, 1001, 1005, 1002, 1004}` and run it again.

```java run
public class Main {
    static Node insert(Node node, int bike) {
        if (node == null) {
            return new Node(bike);
        }
        if (bike < node.bike) {
            node.left = insert(node.left, bike);
        } else if (bike > node.bike) {
            node.right = insert(node.right, bike);
        }
        return node;
    }

    static int height(Node node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public static void main(String[] args) {
        int[] arrivals = {1001, 1002, 1003, 1004, 1005};
        Node root = null;
        for (int bike : arrivals) {
            root = insert(root, bike);
        }
        System.out.println("Bikes: " + arrivals.length);
        System.out.println("Height: " + height(root));
    }
}

class Node {
    int bike;
    Node left;
    Node right;

    Node(int bike) {
        this.bike = bike;
    }
}
```

Sorted arrivals give height 5, a chain. The mixed order gives height 3. Same bikes, very different trees.

Now compare it with the library version, which stays balanced on its own:

```java run
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        TreeMap<Integer, String> bikes = new TreeMap<>();
        bikes.put(1042, "Harbor");
        bikes.put(1003, "Uptown");
        bikes.put(1088, "Riverside");
        bikes.put(1017, "Old Mill");
        System.out.println(bikes.keySet());
        System.out.println(bikes.firstKey() + " to " + bikes.lastKey());
        System.out.println(bikes.get(1017));
    }
}
```

```callout tip
Gojo says: in real code, reach for TreeMap or TreeSet when you need sorted keys and fast lookups. Build the tree by hand only to learn how it works.
```

## Recap

- A tree has a root at the top, children below, and leaves with no children. A binary tree has at most two children per node.
- In a BST, smaller values go left and larger values go right, at every node.
- Insert and search walk one path from the root, so they cost the height of the tree.
- In-order traversal (left, node, right) lists a BST in sorted order.
- A balanced tree gives O(log n). Sorted arrivals build a degenerate chain with O(n). `TreeMap` stays balanced for you.

## Common mistakes

**1. Forgetting to store the result of insert.**

```java nocompile
Node root = null;
insert(root, 1042);
insert(root, 1017);
System.out.println(root.bike);
```

The program crashes with `java.lang.NullPointerException: Cannot read field "bike" because "<local1>" is null`. The helper built a node, but nothing kept it. `root` is still `null`. Fix: write `root = insert(root, 1042);`. Inside the helper, also write `node.left = insert(node.left, bike);`.

**2. Visiting the node before the left subtree and expecting sorted output.**

```java nocompile
static void inOrder(Node node, List<Integer> out) {
    if (node == null) {
        return;
    }
    out.add(node.bike);
    inOrder(node.left, out);
    inOrder(node.right, out);
}
```

This is pre-order. For the bikes 1042, 1017, 1088 it prints `[1042, 1017, 1088]` instead of `[1017, 1042, 1088]`. Fix: call `inOrder(node.left, out)` first, then `out.add(node.bike)`, then the right side.

**3. Forgetting the null check in a recursive method.**

```java nocompile
static int height(Node node) {
    return 1 + Math.max(height(node.left), height(node.right));
}
```

Every leaf calls `height(null)`, so the program crashes with `java.lang.NullPointerException: Cannot read field "left" because "<parameter1>" is null`. Fix: start with `if (node == null) { return 0; }`. Every recursive tree method needs this base case.
