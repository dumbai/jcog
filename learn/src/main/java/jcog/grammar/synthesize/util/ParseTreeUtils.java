













package jcog.grammar.synthesize.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jcog.grammar.synthesize.util.GrammarUtils.*;

public enum ParseTreeUtils {
	;

	public interface ParseTreeNode {
        String getExample();

        Node getNode();

        List<ParseTreeNode> getChildren();
    }

    public static class ParseTreeRepetitionNode implements ParseTreeNode {
        private final RepetitionNode node;
        private final String example;

        public final ParseTreeNode start;
        public final List<ParseTreeNode> rep;
        public final ParseTreeNode end;

        public ParseTreeRepetitionNode(RepetitionNode node, ParseTreeNode start, List<ParseTreeNode> rep, ParseTreeNode end) {
            this.node = node;
            this.start = start;
            this.rep = rep;
            this.end = end;
            StringJoiner joiner = new StringJoiner("", start.getExample(), end.getExample());
            for (ParseTreeNode parseTreeNode : rep) {
                String parseTreeNodeExample = parseTreeNode.getExample();
                joiner.add(parseTreeNodeExample);
            }
            String sb = joiner.toString();
            this.example = sb;
        }

        @Override
        public Node getNode() {
            return this.node;
        }

        @Override
        public String getExample() {
            return this.example;
        }

        @Override
        public List<ParseTreeNode> getChildren() {
            List<ParseTreeNode> children = new ArrayList<>(2 + rep.size());
            children.add(this.start);
            children.addAll(this.rep);
            children.add(this.end);
            return children;
        }

        @Override
        public String toString() {
            return this.example;
        }
    }

    public static class ParseTreeMultiAlternationNode implements ParseTreeNode {
        private final MultiAlternationNode node;
        private final String example;

        public final ParseTreeNode choice;

        public ParseTreeMultiAlternationNode(MultiAlternationNode node, ParseTreeNode choice) {
            this.node = node;
            this.example = choice.getExample();
            this.choice = choice;
        }

        @Override
        public Node getNode() {
            return this.node;
        }

        @Override
        public String getExample() {
            return this.example;
        }

        @Override
        public List<ParseTreeNode> getChildren() {
            return getList(this.choice);
        }

        @Override
        public String toString() {
            return this.example;
        }
    }

    public static class ParseTreeMultiConstantNode implements ParseTreeNode {
        private final MultiConstantNode node;
        private final String example;

        public ParseTreeMultiConstantNode(MultiConstantNode node, String example) {
            this.node = node;
            this.example = example;
        }

        @Override
        public Node getNode() {
            return this.node;
        }

        @Override
        public String getExample() {
            return this.example;
        }

        @Override
        public List<ParseTreeNode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return this.example;
        }
    }

    public static class ParseTreeMergeNode implements ParseTreeNode {
        private final Node node;
        private final String example;

        public final ParseTreeNode merge;

        public ParseTreeMergeNode(Node node, ParseTreeNode merge) {
            this.node = node;
            this.merge = merge;
            this.example = merge.getExample();
        }

        @Override
        public Node getNode() {
            return this.node;
        }

        @Override
        public String getExample() {
            return this.example;
        }

        @Override
        public List<ParseTreeNode> getChildren() {
            return getList(this.merge);
        }

        @Override
        public String toString() {
            return this.example;
        }
    }

    public static List<ParseTreeNode> getParseTreeAlt(MultiAlternationNode node) {
        List<ParseTreeNode> parseTreeNodes = node.getChildren().stream().map(child -> new ParseTreeMultiAlternationNode(node, getParseTreeRepConst(child))).collect(Collectors.toList());
        return parseTreeNodes;
    }

    public static ParseTreeNode getParseTreeRepConst(Node node) {
        if (node instanceof RepetitionNode repNode) {
            ParseTreeNode start = getParseTreeRepConst(repNode.start);
            ParseTreeNode end = getParseTreeRepConst(repNode.end);
            return new ParseTreeRepetitionNode(repNode, start,
                    (repNode.rep instanceof MultiAlternationNode) ?
                            getParseTreeAlt((MultiAlternationNode) repNode.rep)
                                :
                            getList(getParseTreeRepConst(repNode.rep)),
                    end);

        } else if (node instanceof MultiConstantNode) {
            return new ParseTreeMultiConstantNode((MultiConstantNode) node, node.getData().example);
        } else {
            throw new RuntimeException("Invalid node type: " + node.getClass().getName());
        }
    }

    public static ParseTreeNode getParseTree(Node node) {
        return getParseTreeRepConst(node);
    }

    private static void getDescendantsHelper(ParseTreeNode node, List<ParseTreeNode> descendants) {
        descendants.add(node);
        for (ParseTreeNode child : node.getChildren()) {
            getDescendantsHelper(child, descendants);
        }
    }

    public static List<ParseTreeNode> getDescendants(ParseTreeNode node) {
        List<ParseTreeNode> descendants = new ArrayList<>();
        getDescendantsHelper(node, descendants);
        return descendants;
    }

    private static void getDescendantsByTypeHelper(ParseTreeNode node, List<ParseTreeNode>[] descendants) {
        if (node instanceof ParseTreeMultiConstantNode) {
            descendants[0].add(node);
        } else {
            descendants[1].add(node);
        }
        for (ParseTreeNode child : node.getChildren()) {
            getDescendantsByTypeHelper(child, descendants);
        }
    }

    public static List<ParseTreeNode>[] getDescendantsByType(ParseTreeNode node) {
        @SuppressWarnings("unchecked")
        List<ParseTreeNode>[] descendants = IntStream.range(0, 2).<List<ParseTreeNode>>mapToObj(i -> new ArrayList<>()).toArray(List[]::new);
        getDescendantsByTypeHelper(node, descendants);
        return descendants;
    }

    public static ParseTreeNode getSubstitute(ParseTreeNode node, ParseTreeNode cur, ParseTreeNode sub) {
        if (node == cur) {
            return sub;
        } else if (node instanceof ParseTreeRepetitionNode repNode) {

            List<ParseTreeNode> newRep = repNode.rep.stream().map(rep -> getSubstitute(rep, cur, sub)).collect(Collectors.toList());

            return new ParseTreeRepetitionNode(repNode.node,
                    getSubstitute(repNode.start, cur, sub),
                    newRep,
                    getSubstitute(repNode.end, cur, sub));

        } else if (node instanceof ParseTreeMultiAlternationNode pn) {
            return new ParseTreeMultiAlternationNode(pn.node, getSubstitute(pn.choice, cur, sub));
        } else if (node instanceof ParseTreeMultiConstantNode) {
            return node;
        } else {
            throw new RuntimeException("Unrecognized node type: " + node.getClass().getName());
        }
    }
}