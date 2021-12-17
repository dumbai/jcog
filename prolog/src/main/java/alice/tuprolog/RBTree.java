package alice.tuprolog;

import java.util.LinkedList;

/* Copyright (c) 2010 the authors listed at the following URL, and/or
the authors of referenced articles or incorporated external code:
http:

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Retrieved from: http:
*/

enum Color { ;
    static final boolean RED = true;
    static final boolean BLACK = false;
}

/**
 * Implements a Red Black Tree's node.
 * <p>
 * Introduced by Paolo Contessi,
 * retrieved from: http:
 *
 * @param <K> It's the type of the key used to recall values
 * @param <V> It's the type of the values stored in the tree
 * @since 2.2
 */
final class Node<K extends Comparable<? super K>, V> {
    public K key;
    public V value;
    public Node<K, V> left;
    public Node<K, V> right;
    public Node<K, V> parent;
    public boolean color;

    Node(K key, V value, boolean nodeColor, Node<K, V> left, Node<K, V> right) {
        this.key = key;
        this.value = value;
        this.color = nodeColor;
        this.left = left;
        this.right = right;
        if (left != null) left.parent = this;
        if (right != null) right.parent = this;
        this.parent = null;
    }

    public Node<K, V> grandparent() {
        assert parent != null;
        assert parent.parent != null;
        return parent.parent;
    }

    public Node<K, V> sibling() {
        Node<K, V> p = this.parent;
        //assert p != null;
        return this == p.left ? p.right : p.left;
    }

    public Node<K, V> uncle() {
        //assert parent != null && parent.parent != null;
        return parent.sibling();
    }

}

/**
 * Implements a Red Black Tree
 * <p>
 * Introduced by Paolo Contessi,
 * retrieved from: http:
 *
 * @param <K> It's the type of the key used to recall values
 * @param <V> It's the type of the values stored in the tree
 */
public class RBTree<K extends Comparable<? super K>, V> {
    //    public static final boolean VERIFY_RBTREE = false;
    private static final int INDENT_STEP = 4;

    Node<K, V> root;

    public RBTree() {
        root = null;
    }

    private static boolean nodeColor(Node<?, ?> n) {
        return n == null ? Color.BLACK : n.color;
    }

//    public void verifyProperties() {
//        verifyProperty1(root);
//        verifyProperty2(root);
//
//        verifyProperty4(root);
//        verifyProperty5(root);
//    }
//
//    private static void verifyProperty1(Node<?, ?> n) {
//        assert nodeColor(n) == Color.RED || nodeColor(n) == Color.BLACK;
//        if (n == null) return;
//        verifyProperty1(n.left);
//        verifyProperty1(n.right);
//    }
//
//    private static void verifyProperty2(Node<?, ?> root) {
//        assert nodeColor(root) == Color.BLACK;
//    }
//
//    private static void verifyProperty4(Node<?, ?> n) {
//        if (nodeColor(n) == Color.RED) {
//            assert nodeColor(n.left) == Color.BLACK;
//            assert nodeColor(n.right) == Color.BLACK;
//            assert nodeColor(n.parent) == Color.BLACK;
//        }
//        if (n == null) return;
//        verifyProperty4(n.left);
//        verifyProperty4(n.right);
//    }
//
//    private static void verifyProperty5(Node<?, ?> root) {
//        verifyProperty5Helper(root, 0, -1);
//    }
//
//    private static int verifyProperty5Helper(Node<?, ?> n, int blackCount, int pathBlackCount) {
//        if (nodeColor(n) == Color.BLACK) {
//            blackCount++;
//        }
//        if (n == null) {
//            if (pathBlackCount == -1) {
//                pathBlackCount = blackCount;
//            } else {
//                assert blackCount == pathBlackCount;
//            }
//            return pathBlackCount;
//        }
//        pathBlackCount = verifyProperty5Helper(n.left, blackCount, pathBlackCount);
//        pathBlackCount = verifyProperty5Helper(n.right, blackCount, pathBlackCount);
//        return pathBlackCount;
//    }

    private Node<K, V> node(K key) {
        Node<K, V> n = root;
        while (n != null) {
            int c = key.compareTo(n.key);
            if (c == 0) {
                return n;
            } else if (c < 0) {
                n = n.left;
            } else {
                n = n.right;
            }
        }
        return n;
    }

    V lookup(K key) {
        Node<K, V> n = node(key);
        return n == null ? null : n.value;
    }

    private void rotateLeft(Node<K, V> n) {
        Node<K, V> r = n.right;
        replaceNode(n, r);
        Node<K, V> rleft = r.left;
        n.right = rleft;
        if (rleft != null) {
            rleft.parent = n;
        }
        r.left = n;
        n.parent = r;
    }

    private void rotateRight(Node<K, V> n) {
        Node<K, V> l = n.left;
        replaceNode(n, l);
        Node<K, V> lright = l.right;
        n.left = lright;
        if (lright != null) {
            lright.parent = n;
        }
        l.right = n;
        n.parent = l;
    }

    private void replaceNode(Node<K, V> oldn, Node<K, V> newn) {
        Node<K, V> parent = oldn.parent;
        if (parent == null) {
            root = newn;
        } else {
            if (oldn == parent.left)
                parent.left = newn;
            else
                parent.right = newn;
        }
        if (newn != null) {
            newn.parent = parent;
        }
    }

//    public void insert(K key, V value) {
//        Node<K, V> insertedNode = new Node<>(key, value, Color.RED, null, null);
//        if (root == null) {
//            root = insertedNode;
//        } else {
//            Node<K, V> n = root;
//            while (true) {
//                int compResult = key.compareTo(n.key);
//                if (compResult == 0) {
//                    n.value = value;
//                    return;
//                } else if (compResult < 0) {
//                    Node<K, V> nl = n.left;
//                    if (nl == null) {
//                        n.left = insertedNode;
//                        break;
//                    } else {
//                        n = nl;
//                    }
//                } else {
//                    assert compResult > 0;
//                    Node<K, V> nr = n.right;
//                    if (nr == null) {
//                        n.right = insertedNode;
//                        break;
//                    } else {
//                        n = nr;
//                    }
//                }
//            }
//            insertedNode.parent = n;
//        }
//        insertCase1(insertedNode);
//
//    }

    void insertCase1(Node<K, V> n) {
        if (n.parent == null)
            n.color = Color.BLACK;
        else
            insertCase2(n);
    }

    private void insertCase2(Node<K, V> n) {
        if (nodeColor(n.parent) == Color.BLACK) {
        } else
            insertCase3(n);
    }

    private void insertCase3(Node<K, V> n) {
        if (nodeColor(n.uncle()) == Color.RED) {
            n.parent.color = Color.BLACK;
            n.uncle().color = Color.BLACK;
            n.grandparent().color = Color.RED;
            insertCase1(n.grandparent());
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(Node<K, V> n) {
        Node<K, V> p = n.parent;
        Node<K, V> gp = n.grandparent();
        if (n == p.right && p == gp.left) {
            rotateLeft(p);
            insertCase5(n.left);
        } else if (n == p.left && p == gp.right) {
            rotateRight(p);
            insertCase5(n.right);
        }
    }

    private void insertCase5(Node<K, V> n) {
        n.parent.color = Color.BLACK;
        n.grandparent().color = Color.RED;
        if (n == n.parent.left && n.parent == n.grandparent().left) {
            rotateRight(n.grandparent());
        } else {
            //assert n == n.parent.right && n.parent == n.grandparent().right;
            rotateLeft(n.grandparent());
        }
    }

    public void remove(K key, ClauseInfo c) {

        Node<K, V> n = node(key);
        if (n == null)
            return;

        /*must be check if node is a list of clause*/
        @SuppressWarnings("unchecked")
        LinkedList<ClauseInfo> nodeClause = (LinkedList<ClauseInfo>) n.value;
        if (nodeClause.size() > 1) {
            nodeClause.remove(c);
        } else {
            if (n.left != null && n.right != null) {


                Node<K, V> pred = maximumNode(n.left);
                n.key = pred.key;
                n.value = pred.value;
                n = pred;
            }

            assert n.left == null || n.right == null;
            Node<K, V> child = (n.right == null) ? n.left : n.right;

            if (nodeColor(n) == Color.BLACK) {
                n.color = nodeColor(child);
                deleteCase1(n);
            }
            replaceNode(n, child);

            if (nodeColor(root) == Color.RED) {
                root.color = Color.BLACK;
            }


        }
    }

    private static <K extends Comparable<? super K>, V> Node<K, V> maximumNode(Node<K, V> n) {
        assert n != null;
        while (n.right != null) {
            n = n.right;
        }
        return n;
    }

    private void deleteCase1(Node<K, V> n) {
        if (n.parent != null)
            deleteCase2(n);
    }

    private void deleteCase2(Node<K, V> n) {
        if (nodeColor(n.sibling()) == Color.RED) {
            n.parent.color = Color.RED;
            n.sibling().color = Color.BLACK;
            if (n == n.parent.left)
                rotateLeft(n.parent);
            else
                rotateRight(n.parent);
        }
        deleteCase3(n);
    }

    private void deleteCase3(Node<K, V> n) {
        Node<K, V> ns = n.sibling();
        if (nodeColor(n.parent) == Color.BLACK &&
                nodeColor(ns) == Color.BLACK &&
                nodeColor(ns.left) == Color.BLACK &&
                nodeColor(ns.right) == Color.BLACK) {
            ns.color = Color.RED;
            deleteCase1(n.parent);
        } else
            deleteCase4(n);
    }

    private void deleteCase4(Node<K, V> n) {
        Node<K, V> ns = n.sibling();
        if (nodeColor(n.parent) == Color.RED &&
                nodeColor(ns) == Color.BLACK &&
                nodeColor(ns.left) == Color.BLACK &&
                nodeColor(ns.right) == Color.BLACK) {
            ns.color = Color.RED;
            n.parent.color = Color.BLACK;
        } else
            deleteCase5(n);
    }

    private void deleteCase5(Node<K, V> n) {
        Node<K, V> ns = n.sibling();
        if (n == n.parent.left &&
                nodeColor(ns) == Color.BLACK &&
                nodeColor(ns.left) == Color.RED &&
                nodeColor(ns.right) == Color.BLACK) {
            ns.color = Color.RED;
            ns.left.color = Color.BLACK;
            rotateRight(ns);
        } else if (n == n.parent.right &&
                nodeColor(ns) == Color.BLACK &&
                nodeColor(ns.right) == Color.RED &&
                nodeColor(ns.left) == Color.BLACK) {
            ns.color = Color.RED;
            ns.right.color = Color.BLACK;
            rotateLeft(ns);
        }
        deleteCase6(n);
    }

    private void deleteCase6(Node<K, V> n) {
        Node<K, V> ns = n.sibling();
        ns.color = nodeColor(n.parent);
        n.parent.color = Color.BLACK;
        if (n == n.parent.left) {
            assert nodeColor(ns.right) == Color.RED;
            ns.right.color = Color.BLACK;
            rotateLeft(n.parent);
        } else {
            assert nodeColor(ns.left) == Color.RED;
            ns.left.color = Color.BLACK;
            rotateRight(n.parent);
        }
    }

    public void print() {
        printHelper(root, 0);
    }

    private static void printHelper(Node<?, ?> n, int indent) {
        if (n == null) {
            System.out.print("<empty tree>");
            return;
        }
        if (n.right != null) {
            printHelper(n.right, indent + INDENT_STEP);
        }
        for (int i = 0; i < indent; i++)
            System.out.print(" ");
        if (n.color == Color.BLACK)
            System.out.println(n.key);
        else
            System.out.println("<" + n.key + '>');
        if (n.left != null) {
            printHelper(n.left, indent + INDENT_STEP);
        }
    }


}