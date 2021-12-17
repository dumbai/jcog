













package jcog.grammar.synthesize.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum GrammarUtils {
	;

	public static <V> Map<V, Integer> getInverse(List<V> list) {
        int l = list.size();
        Map<V, Integer> inverse = IntStream.range(0, l).boxed().collect(Collectors.toMap(list::get, Function.identity(), (a, b) -> b, () -> new HashMap<>(l)));
        return inverse;
    }

    @SafeVarargs
    public static <T> List<T> getList(T... ts) {


        return List.of(ts);
    }

    public static class Grammar {
        public final Node node;
        public final NodeMerges merges;

        public Grammar(Node node, NodeMerges merges) {
            this.node = node;
            this.merges = merges;
        }
    }

    public static final class Context {
        public final String pre;
        public final String post;
        public final String extraPre;
        public final String extraPost;

        public static final Context EMPTY = new Context();

        private Context() {
            this.pre = "";
            this.post = "";
            this.extraPre = "";
            this.extraPost = "";
        }

        public Context(Context parent, String pre, String post, String extraPre, String extraPost) {
            this.pre = parent.pre + pre;
            this.post = post + parent.post;
            this.extraPre = parent.extraPre + extraPre;
            this.extraPost = extraPost + parent.extraPost;
        }

        public boolean useExtra() {
            return !this.pre.equals(this.extraPre) || !this.post.equals(this.extraPost);
        }
    }

    public static class NodeData {
        public final String example;
        public final Context context;

        public NodeData(String example, Context context) {
            this.example = example;
            this.context = context;
        }
    }

    public interface Node {
        List<Node> getChildren();

        NodeData getData();
    }

    public static class ConstantNode implements Node {
        private final NodeData data;

        public ConstantNode(NodeData data) {
            this.data = data;
        }

        public List<Node> getChildren() {
            return new ArrayList<>();
        }

        public NodeData getData() {
            return this.data;
        }

        public String toString() {
            return this.data.example;
        }
    }

    public static class MultiConstantNode implements Node {
        private final NodeData data;
        public final List<Set<Character>> characterOptions = new ArrayList<>();
        public final List<Set<Character>> characterChecks = new ArrayList<>();

        public MultiConstantNode(NodeData data, Collection<List<Character>> characterOptions, Collection<List<Character>> characterChecks) {
            this.data = data;
            if (characterOptions.size() != characterChecks.size()) {
                throw new RuntimeException("Invalid characters!");
            }
            for (List<Character> characters : characterOptions) {
                this.characterOptions.add(new LinkedHashSet<>(characters));
            }
            for (List<Character> characters : characterChecks) {
                this.characterChecks.add(new LinkedHashSet<>(characters));
            }
        }

        public List<Node> getChildren() {
            return new ArrayList<>();
        }

        public NodeData getData() {
            return this.data;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Set<Character> characterOption : this.characterOptions) {
                sb.append('(');
                for (char character : characterOption) {
                    sb.append(character).append('+');
                }
                sb.replace(sb.length() - 1, sb.length(), ")");
            }
            return sb.toString();
        }
    }

    public static class AlternationNode implements Node {
        private final NodeData data;
        public final Node first;
        public final Node second;

        public AlternationNode(NodeData data, Node first, Node second) {
            this.data = data;
            this.first = first;
            this.second = second;
        }

        public List<Node> getChildren() {
            return List.of(first, second);




        }

        public NodeData getData() {
            return this.data;
        }

        public String toString() {
            return '(' + this.first.toString() + ")+(" + this.second;
        }
    }

    public static class MultiAlternationNode implements Node {
        private final NodeData data;
        private final List<Node> children;

        public MultiAlternationNode(NodeData data, List<Node> children) {
            this.data = data;
            
            this.children = children;
        }

        public List<Node> getChildren() {
            
            return children;
        }

        public NodeData getData() {
            return this.data;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Node child : this.children) {
                sb.append('(').append(child).append(")+");
            }
            return sb.substring(0, sb.length() - 1);
        }
    }

    public static class RepetitionNode implements Node {
        private final NodeData data;
        public final Node start;
        public final Node rep;
        public final Node end;

        public RepetitionNode(NodeData data, Node start, Node rep, Node end) {
            this.data = data;
            this.start = start;
            this.rep = rep;
            this.end = end;
        }

        public List<Node> getChildren() {
            return List.of(start, rep, end);





        }

        public NodeData getData() {
            return this.data;
        }

        public String toString() {
            return this.start.toString() + '(' + this.rep + ")*" + this.end;
        }
    }

    public static class NodeMerges {
        private final MultivalueMap<Node, Node> merges = new MultivalueMap<>();

        public void add(Node first, Node second) {
            this.merges.add(first, second);
            this.merges.add(second, first);
        }

        public void addAll(NodeMerges other) {
            for (Node first : other.keySet()) {
                for (Node second : other.get(first)) {
                    this.add(first, second);
                }
            }
        }

        public Set<Node> get(Node node) {
            return this.merges.get(node);
        }

        public Collection<Node> keySet() {
            return this.merges.keySet();
        }

        public boolean contains(Node first, Node second) {
            return this.merges.get(first).contains(second);
        }
    }

    private static void getAllNodesHelper(Node root, List<Node> nodes) {
        nodes.add(root);
        for (Node child : root.getChildren()) {
            getAllNodesHelper(child, nodes);
        }
    }

    public static List<Node> getAllNodes(Node root) {
        List<Node> nodes = new ArrayList<>();
        getAllNodesHelper(root, nodes);
        return nodes;
    }

    private static void getDescendantsHelper(Node node, List<Node> descendants) {
        descendants.add(node);
        for (Node child : node.getChildren()) {
            getDescendantsHelper(child, descendants);
        }
    }

    public static Iterable<Node> getDescendants(Node node) {
        List<Node> descendants = new ArrayList<>();
        getDescendantsHelper(node, descendants);
        return descendants;
    }

    public static class MultivalueMap<K, V> extends HashMap<K, Set<V>> {
        private static final long serialVersionUID = -6390444829513305915L;

        public void add(K k, V v) {
            ensure(k).add(v);
        }

        public Collection<V> ensure(K k) {
            return super.computeIfAbsent(k, k1 -> new HashSet<>());
        }

        @Override
        public Set<V> get(Object k) {
            Set<V> vSet = super.get(k);
            return vSet == null ? Collections.EMPTY_SET : vSet;
        }
    }

    public static class Maybe<T> {
        private final T t;

        public Maybe(T t) {
            this.t = t;
        }

        public Maybe() {
            this.t = null;
        }

        public T getT() {
            if (this.t != null) {
                return t;
            }
            throw new RuntimeException("Invalid access!");
        }

        public boolean hasT() {
            return this.t != null;
        }




    }
}
