













package jcog.grammar.synthesize;


import jcog.grammar.synthesize.util.GrammarUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static jcog.grammar.synthesize.util.GrammarUtils.*;

public enum GrammarSynthesis {
	;

	private static Node getNode(String example, Predicate<String> oracle) {
        return GrammarTransformer.getTransform(RegexSynthesis.getNode(example, oracle), oracle);
    }

    public static Grammar getGrammarSingle(String example, Predicate<String> oracle) {
        long time = System.currentTimeMillis();
        if (!oracle.test(example)) {
            throw new RuntimeException("Invalid example: " + example);
        }
        RegexSynthesis.logger.info("PROCESSING EXAMPLE:\n" + example);
        Node node = getNode(example, oracle);
        RegexSynthesis.logger.info("SINGLE REGEX TIME: " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
        time = System.currentTimeMillis();
        Grammar grammar = new Grammar(node, MergesSynthesis.getMergesSingle(node, node, oracle));
        RegexSynthesis.logger.info("SINGLE MERGE TIME: " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
        return grammar;
    }

    public static Grammar learn(List<Node> roots, Predicate<String> oracle) {
        long time = System.currentTimeMillis();
        Grammar grammar = new Grammar(new MultiAlternationNode(
                new NodeData(null, Context.EMPTY), roots), MergesSynthesis.getMergesMultiple(roots, oracle));
        RegexSynthesis.logger.info("MULTIPLE MERGE TIME: " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
        return grammar;
    }

    public static Grammar learn(Iterable<String> examples, Predicate<String> oracle) {
        List<Node> roots = new ArrayList<>();
        for (String example : examples) {
            roots.add(getNode(example, oracle));
        }
        return learn(roots, oracle);
    }

    public static Grammar getRegularGrammarMultipleFromRoots(List<Node> roots, Predicate<String> oracle) {
        long time = System.currentTimeMillis();
        Grammar grammar = new Grammar(
                new MultiAlternationNode(
                        new NodeData(null, Context.EMPTY),
                        roots),
                new NodeMerges());
        RegexSynthesis.logger.info("MULTIPLE MERGE TIME: " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
        return grammar;
    }

    public static Grammar getRegularGrammarMultiple(Collection<String> examples, Predicate<String> oracle) {
        List<Node> roots = examples.stream().map(example -> getNode(example, oracle)).collect(Collectors.toList());
        return getRegularGrammarMultipleFromRoots(roots, oracle);
    }

    public static boolean getCheck(Predicate<String> oracle, Context context, Iterable<String> examples) {
        for (String example : examples) {
            if (!oracle.test(context.pre + example + context.post) || (context.useExtra() && !oracle.test(context.extraPre + example + context.extraPost))) {
                return false;
            }
        }
        return true;
    }

    public static GrammarUtils.Maybe<List<Node>> getMultiAlternationRepetitionConstantChildren(Node node, boolean isParentRep) {
        if (!isParentRep) {
            return new GrammarUtils.Maybe<>();
        }
        if (!(node instanceof MultiAlternationNode)) {
            return new GrammarUtils.Maybe<>();
        }
        List<Node> constantChildren = new ArrayList<>();
        for (Node child : node.getChildren()) {
            if (child instanceof RepetitionNode repChild) {
                if (!(repChild.start instanceof ConstantNode) && !(repChild.start instanceof MultiConstantNode)) {
                    return new GrammarUtils.Maybe<>();
                }
                if (!(repChild.rep instanceof ConstantNode) && !(repChild.rep instanceof MultiConstantNode)) {
                    return new GrammarUtils.Maybe<>();
                }
                if (!(repChild.end instanceof ConstantNode) && !(repChild.end instanceof MultiConstantNode)) {
                    return new GrammarUtils.Maybe<>();
                }
                if (!repChild.start.getData().example.isEmpty() || !repChild.end.getData().example.isEmpty()) {
                    return new GrammarUtils.Maybe<>();
                }
                constantChildren.add(repChild.rep);
            } else if (child instanceof ConstantNode) {
                constantChildren.add(child);
            } else if (child instanceof MultiConstantNode) {
                constantChildren.add(child);
            } else {
                return new GrammarUtils.Maybe<>();
            }
        }
        return new GrammarUtils.Maybe<>(constantChildren);
    }
}