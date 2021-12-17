package alice.tuprolog;

import java.util.ArrayDeque;
import java.util.Deque;


/**
 * <code>FamilyClausesIndex</code> enables family clauses indexing
 * in {@link ConcurrentHashClauseIndex}.
 *
 * @author Paolo Contessi
 * @since 2.2
 */
class FamilyClausesIndex<K extends Comparable<? super K>> extends RBTree<K, Deque<ClauseInfo>> {

//    static class Deque<ClauseInfo> extends ArrayDeque<ClauseInfo> {
//
//        //@Deprecated @Nullable Deque<ClauseInfo> shared;
////        public Deque<ClauseInfo>(Deque<ClauseInfo> shared) {
////            this.shared = shared.isEmpty() ? null : shared;
////        }
//
////        @Nullable
////        private Set<ClauseInfo> set = null;
//
//        public Deque<ClauseInfo>(int cap) {
//            super(cap);
//        }
//
//        @Override
//        public void addFirst(ClauseInfo clauseInfo) {
////            set = null;
//            super.addFirst(clauseInfo);
//        }
//
//        @Override
//        public void addLast(ClauseInfo clauseInfo) {
////            set = null;
//            super.addLast(clauseInfo);
//        }
//
//        @Override
//        public boolean remove(Object o) {
////            set = null;
//            return super.remove(o);
//        }
//
//        @Override
//        public ClauseInfo pollFirst() {
////            set = null;
//            return super.pollFirst();
//        }
//
//        @Override
//        public ClauseInfo pollLast() {
////            set = null;
//            return super.pollLast();
//        }
//    }

    private final Deque<ClauseInfo> shared;

    FamilyClausesIndex(){
        super();
        shared = new ArrayDeque<>();
    }

    private Node<K,Deque<ClauseInfo>> createNewNode(K key, ClauseInfo clause, boolean first){
        int vs = shared.size();
        Deque<ClauseInfo> list =
                new ArrayDeque<>(vs + 1);

        if(first){
            list.addFirst(clause);
            if (vs>0)
                list.addAll(shared);
        } else {
            if (vs>0)
                list.addAll(shared);
            list.addLast(clause);
        }
        
        return new Node<>(key, list, Color.RED, null, null);
    }



    /*
     * Voglio memorizzare un riferimento alla clausola, rispettando l'ordine
     * delle clausole
     *
     * Se l'indice non ha nodi?
     * Se aggiungo un nuovo nodo
     */
    public void insertAsShared(ClauseInfo clause, boolean first){
        if(first){
            shared.addFirst(clause);
        } else {
            shared.addLast(clause);
        }

        
        if(root != null){
            if (root.left==null && root.right == null) {
                
                setValue(clause, first, root);
            } else {
                

                Deque<Node<K, Deque<ClauseInfo>>> buf = new ArrayDeque<>();
                buf.add(root);

                while (!buf.isEmpty()) {
                    Node<K, Deque<ClauseInfo>> n = buf.removeFirst();

                    setValue(clause, first, n);

                    if (n.left != null) {
                        buf.addLast(n.left);
                    }

                    if (n.right != null) {
                        buf.addLast(n.right);
                    }
                }
            }
        }
    }

    private void setValue(ClauseInfo clause, boolean first, Node<K, Deque<ClauseInfo>> n) {
        if(first){
            n.value.addFirst(clause);
        } else {
            n.value.addLast(clause);
        }
    }

    /**
     * Creates a new entry (<code>key</code>) in the index, relative to the
     * given <code>clause</code>. If other clauses is associated to <code>key</code>
     * <code>first</code> parameter is used to decide if it is the first or
     * the last clause to be retrieved.
     *
     * @param key       The key of the index
     * @param clause    The value to be binded to the given key
     * @param first     If the clause must be binded as first or last element
     */
    protected void insert(K key, ClauseInfo clause, boolean first){
        Node<K, Deque<ClauseInfo>> insertedNode;
        if (root == null) {
            insertedNode = root = createNewNode(key, clause, first);
        } else {
            Node<K,Deque<ClauseInfo>> n = root;
            while (true) {
                int compResult = key.compareTo(n.key);
                if (compResult == 0) {
                    setValue(clause, first, n);
                    return;
                } else if (compResult < 0) {
                    if (n.left == null) {
                        insertedNode = n.left = createNewNode(key, clause,first);
                        break;
                    } else {
                        n = n.left;
                    }
                } else {
                    
                    if (n.right == null) {
                        insertedNode = n.right = createNewNode(key, clause,first);
                        break;
                    } else {
                        n = n.right;
                    }
                }
            }
            
            insertedNode.parent = n;
        }
        insertCase1(insertedNode);
        
    }


    protected void removeShared(ClauseInfo clause){
        if(shared.remove(clause)){
            if(root != null){
                if (root.left == null && root.right == null) {
                    root.value.remove(clause);  
                } else {
                    

                    Deque<Node<K, Deque<ClauseInfo>>> buf = new ArrayDeque<>();
                    buf.add(root);

                    while (!buf.isEmpty()) {
                        Node<K, Deque<ClauseInfo>> n = buf.remove();

                        n.value.remove(clause);

                        if (n.left != null) {
                            buf.addLast(n.left);
                        }

                        if (n.right != null) {
                            buf.addLast(n.right);
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid clause: not registered in this index");
        }
    }

    /**
     * Retrieves all the clauses related to the key
     *
     * @param key   The key
     * @return      The related clauses
     */
    public Deque<ClauseInfo> get(K key){
        Deque<ClauseInfo> res = lookup(key);
        return res != null ? res : shared;
    }
}