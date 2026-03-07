/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author asus-z
 * @param <K>
 * @param <V>
 */
public class BPlusTree<K extends Comparable<K>, V> {

    private Node<K, V> root;
    private int M = 3;

    // replace ArrayList，use for return result
    public static class SimpleList<T> {

        private Object[] elements = new Object[10];
        private int size = 0;

        public void add(T item) {
            if (size == elements.length) {
                Object[] newArr = new Object[elements.length * 2];
                System.arraycopy(elements, 0, newArr, 0, size);
                elements = newArr;
            }
            elements[size++] = item;
        }

        public int size() {
            return size;
        }

        @SuppressWarnings("unchecked")
        public T get(int i) {
            return (T) elements[i];
        }
    }

    // replace Queue，use in printTree
    private static class SimpleQueue<T> {

        private Object[] data = new Object[1000];
        private int head = 0, tail = 0;

        public void add(T item) {
            data[tail++] = item;
        }

        public T poll() {
            return (T) data[head++];
        }

        public boolean isEmpty() {
            return head == tail;
        }

        public int size() {
            return tail - head;
        }
    }

    // --- Constructor ---
    public BPlusTree(int m) {
        if (m < 3) {
            throw new IllegalArgumentException("M must be at least 3.");
        }
        this.M = m;
        this.root = new LeafNode<>(M);
    }

    public BPlusTree() {
        this.root = new LeafNode<>(M);
    }

    // --- (CRUD) ---
    public void create(K key, V value) {
        LeafNode<K, V> leaf = findLeaf(root, key);
        int idx = binarySearch(leaf.keys, leaf.currentKeyCount, key);

        if (idx >= 0) {
            leaf.values[idx] = value; // Update if exists
            return;
        }

        int pos = -(idx + 1);
        leaf.insertAt(pos, key, value);

        if (leaf.isOverflow(M)) {
            splitLeaf(leaf);
        }
    }

    public V read(K key) {
        return search(key);
    }

    public void update(K key, V value) {
        create(key, value); // In B+ tree，operator Update is same with Create
    }

    public void delete(K key) {
        LeafNode<K, V> leaf = findLeaf(root, key);
        int idx = binarySearch(leaf.keys, leaf.currentKeyCount, key);

        if (idx >= 0) {
            leaf.removeAt(idx);
            // 如果是根节点，即便只有一个元素也是合法的，除非根变为空
            if (leaf == root) {
                if (leaf.currentKeyCount == 0) {
                    root = null;
                }
                return;
            }

            // 检查是否发生 Underflow (Key 数量少于 Math.ceil(M/2.0) - 1)
            if (leaf.isUnderflow(M)) {
                handleUnderflow(leaf);
            }
        }
    }

    private void handleUnderflow(Node<K, V> node) {
        InternalNode<K, V> parent = (InternalNode<K, V>) node.parent;
        int childIdx = parent.indexOfChild(node);

        // 1. 尝试向左兄弟借 (Borrow from Left Sibling)
        if (childIdx > 0) {
            Node<K, V> leftSibling = parent.children[childIdx - 1];
            if (leftSibling.canLend(M)) {
                borrowFromLeft(node, leftSibling, parent, childIdx);
                return;
            }
        }

        // 2. 尝试向右兄弟借 (Borrow from Right Sibling)
        if (childIdx < parent.currentKeyCount - 1) {
            Node<K, V> rightSibling = parent.children[childIdx + 1];
            if (rightSibling.canLend(M)) {
                borrowFromRight(node, rightSibling, parent, childIdx);
                return;
            }
        }

        // 3. 如果借不到，执行合并 (Merge)
        if (childIdx > 0) {
            // 与左兄弟合并
            merge(parent.children[childIdx - 1], node, parent, childIdx);
        } else {
            // 与右兄弟合并
            merge(node, parent.children[childIdx + 1], parent, childIdx + 1);
        }
    }

    // 具体的合并逻辑实现（简化版示例）
    private void merge(Node<K, V> left, Node<K, V> right, InternalNode<K, V> parent, int rightIdx) {
        // 1. 获取父节点中夹在 left 和 right 之间的那个 Key
        K parentKey = parent.keys[rightIdx - 1];

        // 2. 执行合并
        left.combineWith(right, parentKey);

        // 3. 从父节点中移除掉那个已经下移并失去右孩子的 key
        parent.removeKeyAndChildAt(rightIdx - 1);

        // 4. 递归处理父节点的 Underflow
        if (parent != root && parent.isUnderflow(M)) {
            handleUnderflow(parent);
        } else if (parent == root && parent.currentKeyCount == 0) {
            // 如果根节点被吸干了，让 left 成为新的根
            root = left;
            root.parent = null;
        }
    }

    // --- 查询与排序 ---
    public V search(K key) {
        if (key == null) {
            return null;
        }
        LeafNode<K, V> leaf = findLeaf(root, key);
        int idx = binarySearch(leaf.keys, leaf.currentKeyCount, key);
        return (idx >= 0) ? leaf.values[idx] : null;
    }

    public SimpleList<V> searchRange(K low, K high) {
        SimpleList<V> result = new SimpleList<>();
        LeafNode<K, V> curr = findLeaf(root, low);

        while (curr != null) {
            for (int i = 0; i < curr.currentKeyCount; i++) {
                K k = curr.keys[i];
                if (k.compareTo(high) > 0) {
                    return result;
                }
                if (k.compareTo(low) >= 0) {
                    result.add(curr.values[i]);
                }
            }
            curr = curr.next; // 沿着链表向右走
        }
        return result;
    }

    public SimpleList<V> sort() {
        SimpleList<V> result = new SimpleList<>();
        // 找到最左边的叶子节点
        Node<K, V> curr = root;
        while (curr instanceof InternalNode) {
            curr = ((InternalNode<K, V>) curr).children[0];
        }
        // 遍历双向链表
        LeafNode<K, V> leaf = (LeafNode<K, V>) curr;
        while (leaf != null) {
            for (int i = 0; i < leaf.currentKeyCount; i++) {
                result.add(leaf.values[i]);
            }
            leaf = leaf.next;
        }
        return result;
    }

    // --- 内部逻辑：分裂与提升 ---
    private void splitLeaf(LeafNode<K, V> leaf) {
        int mid = leaf.currentKeyCount / 2;
        int numToMove = leaf.currentKeyCount - mid;
        LeafNode<K, V> sibling = new LeafNode<>(M);

        for (int i = 0; i < numToMove; i++) {
            sibling.keys[i] = leaf.keys[mid + i];
            sibling.values[i] = leaf.values[mid + i];
            leaf.keys[mid + i] = null;
            leaf.values[mid + i] = null;
        }
        sibling.currentKeyCount = numToMove;
        leaf.currentKeyCount = mid;

        // 双向链表连接逻辑
        sibling.next = leaf.next;
        if (leaf.next != null) {
            leaf.next.prev = sibling;
        }
        leaf.next = sibling;
        sibling.prev = leaf;

        pushUp(leaf, sibling, sibling.keys[0]);
    }

    private void pushUp(Node<K, V> left, Node<K, V> right, K key) {
        if (left.parent == null) {
            InternalNode<K, V> newRoot = new InternalNode<>(M);
            newRoot.keys[0] = key;
            newRoot.children[0] = left;
            newRoot.children[1] = right;
            newRoot.currentKeyCount = 1;
            left.parent = newRoot;
            right.parent = newRoot;
            this.root = newRoot;
            return;
        }

        InternalNode<K, V> parent = (InternalNode<K, V>) left.parent;
        int idx = binarySearch(parent.keys, parent.currentKeyCount, key);
        int pos = -(idx + 1);

        parent.insertAt(pos, key, right);
        if (parent.isOverflow(M)) {
            splitInternal(parent);
        }
    }

    private void splitInternal(InternalNode<K, V> node) {
        int mid = node.currentKeyCount / 2;
        K upKey = node.keys[mid];
        InternalNode<K, V> sibling = new InternalNode<>(M);

        int numToMove = node.currentKeyCount - mid - 1;
        for (int i = 0; i < numToMove; i++) {
            sibling.keys[i] = node.keys[mid + 1 + i];
            sibling.children[i] = node.children[mid + 1 + i];
            sibling.children[i].parent = sibling;
            node.keys[mid + 1 + i] = null;
            node.children[mid + 1 + i] = null;
        }
        sibling.children[numToMove] = node.children[node.currentKeyCount];
        sibling.children[numToMove].parent = sibling;
        node.children[node.currentKeyCount] = null;

        sibling.currentKeyCount = numToMove;
        node.currentKeyCount = mid;

        pushUp(node, sibling, upKey);
    }

    private LeafNode<K, V> findLeaf(Node<K, V> node, K key) {
        if (node instanceof LeafNode) {
            return (LeafNode<K, V>) node;
        }

        InternalNode<K, V> internal = (InternalNode<K, V>) node;
        int idx = binarySearch(internal.keys, internal.currentKeyCount, key);
        int childIdx = (idx >= 0) ? idx + 1 : -(idx + 1);
        return findLeaf(internal.children[childIdx], key);
    }

    private int binarySearch(K[] arr, int count, K key) {
        int low = 0, high = count - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = arr[mid].compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -(low + 1);
    }

    private void borrowFromLeft(Node<K, V> node, Node<K, V> leftSibling, InternalNode<K, V> parent, int childIdx) {
        if (node instanceof LeafNode) {
            LeafNode<K, V> leaf = (LeafNode<K, V>) node;
            LeafNode<K, V> left = (LeafNode<K, V>) leftSibling;

            // 1. 从左兄弟拿走最后一个 key 和 value
            K borrowedKey = left.keys[left.currentKeyCount - 1];
            V borrowedValue = left.values[left.currentKeyCount - 1];
            left.removeAt(left.currentKeyCount - 1);

            // 2. 插入到当前叶子节点的最前面
            leaf.insertAt(0, borrowedKey, borrowedValue);

            // 3. 更新父节点中的分隔符 (Separator)
            parent.keys[childIdx - 1] = leaf.keys[0];

        } else {
            InternalNode<K, V> internal = (InternalNode<K, V>) node;
            InternalNode<K, V> left = (InternalNode<K, V>) leftSibling;

            // 1. 内部节点旋转：父节点的 key 下移到当前节点最前面
            K parentKey = parent.keys[childIdx - 1];
            Node<K, V> borrowedChild = left.children[left.currentKeyCount]; // 左兄弟最后的儿子

            internal.insertKeyAndChildAtFirst(parentKey, borrowedChild);

            // 2. 左兄弟最后的 key 上升到父节点
            parent.keys[childIdx - 1] = left.keys[left.currentKeyCount - 1];

            // 3. 移除左兄弟多余的尾巴
            left.removeLast();
        }
    }

    private void borrowFromRight(Node<K, V> node, Node<K, V> rightSibling, InternalNode<K, V> parent, int childIdx) {
        if (node instanceof LeafNode) {
            LeafNode<K, V> leaf = (LeafNode<K, V>) node;
            LeafNode<K, V> right = (LeafNode<K, V>) rightSibling;

            // 1. 从右兄弟拿走第一个 key 和 value
            K borrowedKey = right.keys[0];
            V borrowedValue = right.values[0];
            right.removeAt(0);

            // 2. 放到当前节点末尾
            leaf.insertAt(leaf.currentKeyCount, borrowedKey, borrowedValue);

            // 3. 更新父节点的 key 为右兄弟现在新的第一个值
            parent.keys[childIdx] = right.keys[0];

        } else {
            InternalNode<K, V> internal = (InternalNode<K, V>) node;
            InternalNode<K, V> right = (InternalNode<K, V>) rightSibling;

            // 1. 内部节点旋转：父节点 key 下移到当前节点末尾
            K parentKey = parent.keys[childIdx];
            Node<K, V> borrowedChild = right.children[0];

            internal.insertAt(internal.currentKeyCount, parentKey, borrowedChild);

            // 2. 右兄弟第一个 key 上升到父节点
            parent.keys[childIdx] = right.keys[0];

            // 3. 右兄弟整体前移（删除第一个 key 和 child）
            right.removeFirst(); // 你可能需要在 InternalNode 补一个 removeFirst
        }
    }

    public void printTree() {
        SimpleQueue<Node<K, V>> queue = new SimpleQueue<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Node<K, V> node = queue.poll();
                // 打印当前节点的所有 Key
                System.out.print("[");
                for (int j = 0; j < node.currentKeyCount; j++) {
                    System.out.print(node.keys[j] + (j == node.currentKeyCount - 1 ? "" : ", "));
                }
                System.out.print("] ");

                if (node instanceof InternalNode) {
                    InternalNode<K, V> in = (InternalNode<K, V>) node;
                    for (int j = 0; j <= in.currentKeyCount; j++) {
                        if (in.children[j] != null) {
                            queue.add(in.children[j]);
                        }
                    }
                }
            }
            System.out.println();
        }
    }
}
