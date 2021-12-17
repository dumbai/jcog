













package jcog.grammar.synthesize;

import jcog.grammar.synthesize.util.CharacterUtils;
import jcog.grammar.synthesize.util.GrammarUtils;
import jcog.grammar.synthesize.util.GrammarUtils.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum GrammarTransformer {
	;

	public static Node getTransform(Node node, Predicate<String> oracle) {
        Node transformFlatten = getTransform(node, new FlattenTransformer());
        return getTransform(transformFlatten, new ConstantTransformer(oracle, getMultiAlternationRepetitionConstantNodes(transformFlatten)));
    }

    private interface NodeTransformer {
        Node transformConstant(ConstantNode node);

        Node transformMultiConstant(MultiConstantNode node);

        Node transformAlternation(AlternationNode node, Node newFirst, Node newSecond);

        Node transformRepetition(RepetitionNode node, Node newStart, Node newRep, Node newEnd);

        Node transformMultiAlternation(MultiAlternationNode node, List<Node> newChildren);
    }

    private static Node getTransform(Node node, NodeTransformer transformer) {
        if (node instanceof ConstantNode) {
            return transformer.transformConstant((ConstantNode) node);
        } else if (node instanceof MultiConstantNode) {
            return transformer.transformMultiConstant((MultiConstantNode) node);
        } else if (node instanceof AlternationNode altNode) {
            Node newFirst = getTransform(altNode.first, transformer);
            Node newSecond = getTransform(altNode.second, transformer);
            return transformer.transformAlternation(altNode, newFirst, newSecond);
        } else if (node instanceof MultiAlternationNode) {
            List<Node> newChildren = node.getChildren().stream().map(child -> getTransform(child, transformer)).collect(Collectors.toList());
            return transformer.transformMultiAlternation((MultiAlternationNode) node, newChildren);
        } else if (node instanceof RepetitionNode repNode) {
            Node newStart = getTransform(repNode.start, transformer);
            Node newRep = getTransform(repNode.rep, transformer);
            Node newEnd = getTransform(repNode.end, transformer);
            return transformer.transformRepetition(repNode, newStart, newRep, newEnd);
        } else {
            throw new RuntimeException("Invalid node type: " + node.getClass().getName());
        }
    }

    private static MultiConstantNode generalizeConstant(Node node, Predicate<String> oracle) {
        String example = node.getData().example;
        Context context = node.getData().context;
        if (!example.isEmpty()) {
            RegexSynthesis.logger.info("GENERALIZING CONST: " + example + " ## " + context.pre + " ## " + context.post);
        }
        List<List<Character>> characterOptions = new ArrayList<>();
        List<List<Character>> characterChecks = new ArrayList<>();
        for (int i = 0; i < example.length(); i++) {
            List<Character> characterOption = new ArrayList<>();
            char curC = example.charAt(i);
            Context curContext = new Context(context, example.substring(0, i), example.substring(i + 1), example.substring(0, i), example.substring(i + 1));
            characterOption.add(curC);
            List<Character> characterCheck = new ArrayList<>();
            characterCheck.add(curC);
            for (CharacterUtils.CharacterGeneralization generalization : CharacterUtils.getGeneralizations()) {
                if (generalization.triggers.contains(curC)) {
                    Collection<String> checks = new ArrayList<>();
                    for (char c : generalization.checks) {
                        if (curC != c) {
                            checks.add(String.valueOf(c));
                        }
                    }
                    if (GrammarSynthesis.getCheck(oracle, curContext, checks)) {
                        for (char c : generalization.characters) {
                            if (curC != c) {
                                characterOption.add(c);
                            }
                        }
                        for (char c : generalization.checks) {
                            if (curC != c) {
                                characterCheck.add(c);
                            }
                        }
                    }
                }
            }
            characterOptions.add(characterOption);
            characterChecks.add(characterCheck);
        }
        return new MultiConstantNode(node.getData(), characterOptions, characterChecks);
    }

    private static boolean isContained(String example, MultiConstantNode mconstNode) {
        int elen = example.length();
        if (elen != mconstNode.characterOptions.size()) {
            return false;
        }
        return IntStream.range(0, elen).allMatch(i -> mconstNode.characterOptions.get(i).contains(example.charAt(i)));
    }

    private static boolean isContained(String example, Iterable<MultiConstantNode> mconstNodes) {
        for (MultiConstantNode mconstNode : mconstNodes) {
            if (isContained(example, mconstNode)) {
                return true;
            }
        }
        return false;
    }

    private static Node generalizeMultiAlternationConstant(MultiAlternationNode node, GrammarUtils.MultivalueMap<MultiAlternationNode, ConstantNode> multiAlternationNodeConstantChildren, Predicate<String> oracle) {
        RegexSynthesis.logger.info("GENERALIZING MULTI ALT: " + node.getData().example);
        List<MultiConstantNode> curConsts = new ArrayList<>();
        for (Node child : multiAlternationNodeConstantChildren.get(node)) {
            if (!isContained(child.getData().example, curConsts)) {
                curConsts.add(generalizeConstant(child, oracle));
            }
        }
        return new MultiAlternationNode(node.getData(), new ArrayList<>(curConsts));
    }

    private static class ConstantTransformer implements NodeTransformer {
        private final Predicate<String> oracle;
        private final GrammarUtils.MultivalueMap<MultiAlternationNode, ConstantNode> multiAlternationNodeConstantChildren;
        private final Collection<ConstantNode> ignoredConstants = new HashSet<>();

        private ConstantTransformer(Predicate<String> oracle, GrammarUtils.MultivalueMap<MultiAlternationNode, ConstantNode> multiAlternationNodeConstantChildren) {
            this.oracle = oracle;
            this.multiAlternationNodeConstantChildren = multiAlternationNodeConstantChildren;
            for (Map.Entry<MultiAlternationNode, Set<ConstantNode>> multiAlternationNodeSetEntry : multiAlternationNodeConstantChildren.entrySet()) {
                this.ignoredConstants.addAll(multiAlternationNodeSetEntry.getValue());
            }
        }

        public Node transformConstant(ConstantNode node) {
            return this.ignoredConstants.contains(node) ? node : generalizeConstant(node, this.oracle);
        }

        public Node transformMultiConstant(MultiConstantNode node) {
            throw new RuntimeException("Invalid node: " + node);
        }

        public Node transformAlternation(AlternationNode node, Node newFirst, Node newSecond) {
            return new AlternationNode(node.getData(), newFirst, newSecond);
        }

        public Node transformMultiAlternation(MultiAlternationNode node, List<Node> newChildren) {
            return this.multiAlternationNodeConstantChildren.containsKey(node) ? generalizeMultiAlternationConstant(node, this.multiAlternationNodeConstantChildren, this.oracle) : new MultiAlternationNode(node.getData(), newChildren);
        }

        public Node transformRepetition(RepetitionNode node, Node newStart, Node newRep, Node newEnd) {
            return new RepetitionNode(node.getData(), newStart, newRep, newEnd);
        }
    }

    private static class FlattenTransformer implements NodeTransformer {
        public Node transformConstant(ConstantNode node) {
            return node;
        }

        public Node transformMultiConstant(MultiConstantNode node) {
            return node;
        }

        public Node transformAlternation(AlternationNode node, Node newFirst, Node newSecond) {
            List<Node> newChildren = new ArrayList<>();
            if (newFirst instanceof MultiAlternationNode) {
                newChildren.addAll(newFirst.getChildren());
            } else {
                newChildren.add(newFirst);
            }
            if (newSecond instanceof MultiAlternationNode) {
                newChildren.addAll(newSecond.getChildren());
            } else {
                newChildren.add(newSecond);
            }
            return new MultiAlternationNode(node.getData(), newChildren);
        }

        public Node transformMultiAlternation(MultiAlternationNode node, List<Node> newChildren) {
            throw new RuntimeException("Invalid node: " + node);
        }

        public Node transformRepetition(RepetitionNode node, Node newStart, Node newRep, Node newEnd) {
            return new RepetitionNode(node.getData(), newStart, newRep, newEnd);
        }
    }

    private static void getMultiAlternationRepetitionConstantNodesHelper(Node node, GrammarUtils.MultivalueMap<MultiAlternationNode, ConstantNode> result, boolean isParentRep) {
        GrammarUtils.Maybe<List<Node>> constantChildren = GrammarSynthesis.getMultiAlternationRepetitionConstantChildren(node, isParentRep);
        if (constantChildren.hasT()) {
            for (Node child : constantChildren.getT()) {
                result.add((MultiAlternationNode) node, (ConstantNode) child);
            }
        } else if (node instanceof RepetitionNode repNode) {
            getMultiAlternationRepetitionConstantNodesHelper(repNode.start, result, false);
            getMultiAlternationRepetitionConstantNodesHelper(repNode.rep, result, true);
            getMultiAlternationRepetitionConstantNodesHelper(repNode.end, result, false);
        } else {
            for (Node child : node.getChildren()) {
                getMultiAlternationRepetitionConstantNodesHelper(child, result, false);
            }
        }
    }

    private static GrammarUtils.MultivalueMap<MultiAlternationNode, ConstantNode> getMultiAlternationRepetitionConstantNodes(Node root) {
        GrammarUtils.MultivalueMap<MultiAlternationNode, ConstantNode> result = new GrammarUtils.MultivalueMap<>();
        getMultiAlternationRepetitionConstantNodesHelper(root, result, false);
        return result;
    }
}