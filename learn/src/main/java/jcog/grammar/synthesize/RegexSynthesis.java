













package jcog.grammar.synthesize;


import jcog.grammar.synthesize.util.GrammarUtils.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public enum RegexSynthesis {
	;

    public static final Logger logger = jcog.Log.log(RegexSynthesis.class);

    public static Node getNode(String example, Predicate<String> oracle) {
        return getNode(new NodeData(example, Context.EMPTY), oracle, NodeType.values(), true);
    }

    private static Iterable<String> getAlternationChecks(String first, String second) {
        List<String> checks = new ArrayList<>();
        checks.add(second + first + second + first);
        checks.add(first + second + first + second);
        checks.add(second + first);
        checks.add(second + second);
        checks.add(first + first);
        checks.add(second);
        checks.add(first);
        checks.add("");
        return checks;
    }

    private static Iterable<String> getRepetitionChecks(String start, String rep, String end) {
        List<String> checks = new ArrayList<>();
        checks.add(start + rep + rep + end);
        checks.add(start + end);
        return checks;
    }

    private static class AlternationPartialNode {
        private final NodeData first;
        private final NodeData second;

        private AlternationPartialNode(NodeData first, NodeData second) {
            this.first = first;
            this.second = second;
        }
    }

    private static class RepetitionPartialNode {
        private final NodeData start;
        private final NodeData rep;
        private final NodeData end;

        private RepetitionPartialNode(NodeData start, NodeData rep, NodeData end) {
            this.start = start;
            this.rep = rep;
            this.end = end;
        }
    }

    private static Maybe<AlternationPartialNode> getAlternationPartialNode(NodeData cur, Predicate<String> oracle) {
        for (int i = 1; i <= cur.example.length() - 1; i++) {
            String first = cur.example.substring(0, i);
            String second = cur.example.substring(i);
            if (GrammarSynthesis.getCheck(oracle, cur.context, getAlternationChecks(first, second))) {
                NodeData firstData = new NodeData(first, new Context(cur.context, "", second, "", ""));
                NodeData secondData = new NodeData(second, new Context(cur.context, first, "", "", ""));
                logger.info("FOUND ALT: {} ## {}", first, second);
                return new Maybe<>(new AlternationPartialNode(firstData, secondData));
            }
        }
        return new Maybe<>();
    }

    private static Maybe<RepetitionPartialNode> getRepetitionPartialNode(NodeData cur, Predicate<String> oracle, boolean isWholeStringRepeatable) {
        for (int init = 0; init <= cur.example.length() - 1; init++) {
            for (int len = cur.example.length() - init; len >= 1; len--) {
                if (len == cur.example.length() && !isWholeStringRepeatable) {
                    continue;
                }
                String start = cur.example.substring(0, init);
                String rep = cur.example.substring(init, init + len);
                String end = cur.example.substring(init + len);
                if (GrammarSynthesis.getCheck(oracle, cur.context, getRepetitionChecks(start, rep, end))) {
                    NodeData startData = new NodeData(start, new Context(cur.context, "", rep + end, "", end));
                    NodeData repData = new NodeData(rep, new Context(cur.context, start, end, start, end));
                    NodeData endData = new NodeData(end, new Context(cur.context, start + rep, "", start, ""));
                    logger.info("FOUND REP: {} ## {} ## {}", rep, start, end);
                    return new Maybe<>(new RepetitionPartialNode(startData, repData, endData));
                }
            }
        }
        return new Maybe<>();
    }

    private static Maybe<Node> getConstantNode(NodeData cur, Predicate<String> oracle) {
        return new Maybe<>(new ConstantNode(cur));
    }

    private static Maybe<Node> getAlternationNode(NodeData cur, Predicate<String> oracle) {
        Maybe<AlternationPartialNode> maybe = getAlternationPartialNode(cur, oracle);
        if (!maybe.hasT()) {
            return new Maybe<>();
        }
        Node first = getNode(maybe.getT().first, oracle, new NodeType[]{NodeType.REPETITION}, true);
        Node second = getNode(maybe.getT().second, oracle, new NodeType[]{NodeType.ALTERNATION, NodeType.REPETITION}, true);
        return new Maybe<>(new AlternationNode(cur, first, second));
    }

    static final NodeType[] emptyNodeTypes = {};

    private static Maybe<Node> getRepetitionNode(NodeData cur, Predicate<String> oracle, boolean isWholeStringRepeatable) {
        Maybe<RepetitionPartialNode> maybe = getRepetitionPartialNode(cur, oracle, isWholeStringRepeatable);
        if (!maybe.hasT()) {
            return new Maybe<>();
        }

        Node start = getNode(maybe.getT().start, oracle, emptyNodeTypes, true);
        Node rep = getNode(maybe.getT().rep, oracle, new NodeType[]{NodeType.ALTERNATION, NodeType.REPETITION}, false);
        Node end = getNode(maybe.getT().end, oracle, new NodeType[]{NodeType.REPETITION}, true);
        return new Maybe<>(new RepetitionNode(cur, start, rep, end));
    }

    private enum NodeType {
        REPETITION, ALTERNATION
    }

    private static Node getNode(NodeData cur, Predicate<String> oracle, NodeType[] types, boolean isWholeStringRepeatable) {
        for (NodeType type : types) {
            switch (type) {
                case REPETITION -> {
                    Maybe<Node> nodeRep = getRepetitionNode(cur, oracle, isWholeStringRepeatable);
                    if (nodeRep.hasT()) {
                        return nodeRep.getT();
                    }
                }
                case ALTERNATION -> {
                    Maybe<Node> nodeAlt = getAlternationNode(cur, oracle);
                    if (nodeAlt.hasT()) {
                        return nodeAlt.getT();
                    }
                }
            }
        }
        return getConstantNode(cur, oracle).getT();
    }
}