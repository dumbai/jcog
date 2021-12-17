













package jcog.grammar.synthesize;

import jcog.grammar.synthesize.util.GrammarUtils;
import jcog.grammar.synthesize.util.GrammarUtils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum GrammarSerializer {
	;

	public static void serialize(CharSequence string, DataOutput dos) throws IOException {
        if (string == null) {
            dos.writeInt(-1);
        } else {
            dos.writeInt(string.length());
            for (int i = 0; i < string.length(); i++) {
                dos.writeChar(string.charAt(i));
            }
        }
    }

    public static String deserializeString(DataInput dis) throws IOException {
        int length = dis.readInt();
        if (length == -1) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(dis.readChar());
            }
            return sb.toString();
        }
    }

    public static void serialize(NodeData data, DataOutputStream dos) throws IOException {
        serialize(data.example, dos);
        serialize(data.context.pre, dos);
        serialize(data.context.post, dos);
        serialize(data.context.extraPre, dos);
        serialize(data.context.extraPost, dos);
    }

    public static NodeData deserializeNodeData(DataInputStream dis) throws IOException {
        String example = deserializeString(dis);
        String pre = deserializeString(dis);
        String post = deserializeString(dis);
        String extraPre = deserializeString(dis);
        String extraPost = deserializeString(dis);
        return new NodeData(example, new Context(Context.EMPTY, pre, post, extraPre, extraPost));
    }

    public static void serialize(Grammar grammar, DataOutputStream dos) throws IOException {
        List<Node> nodes = GrammarUtils.getAllNodes(grammar.node);
        Map<Node, Integer> nodeIds = GrammarUtils.getInverse(nodes);
        dos.writeInt(nodes.size()); 
        for (Node node : nodes) {
            dos.writeInt(nodeIds.get(node)); 
            serialize(node.getData(), dos); 
            if (node instanceof ConstantNode) {
                dos.writeInt(0); 
            } else if (node instanceof AlternationNode altNode) {
                dos.writeInt(1);
                dos.writeInt(nodeIds.get(altNode.first)); 
                dos.writeInt(nodeIds.get(altNode.second)); 
            } else if (node instanceof MultiAlternationNode) {
                dos.writeInt(2); 
                dos.writeInt(node.getChildren().size()); 
                for (Node child : node.getChildren()) {
                    dos.writeInt(nodeIds.get(child)); 
                }
            } else if (node instanceof RepetitionNode repNode) {
                dos.writeInt(3);
                dos.writeInt(nodeIds.get(repNode.start)); 
                dos.writeInt(nodeIds.get(repNode.rep)); 
                dos.writeInt(nodeIds.get(repNode.end)); 
            } else if (node instanceof MultiConstantNode mconstNode) {
                dos.writeInt(4);
                dos.writeInt(mconstNode.characterOptions.size()); 
                for (int i = 0; i < mconstNode.characterOptions.size(); i++) {
                    Set<Character> characterOption = mconstNode.characterOptions.get(i);
                    dos.writeInt(characterOption.size()); 
                    for (char c : characterOption) {
                        dos.writeChar(c); 
                    }
                    Set<Character> characterChecks = mconstNode.characterChecks.get(i);
                    dos.writeInt(characterChecks.size()); 
                    for (char c : characterChecks) {
                        dos.writeChar(c); 
                    }
                }
            } else {
                throw new RuntimeException("Unrecognized node type: " + node.getClass().getName());
            }
        }
        dos.writeInt(grammar.merges.keySet().size()); 
        for (Node first : grammar.merges.keySet()) {
            dos.writeInt(grammar.merges.get(first).size()); 
            for (Node second : grammar.merges.get(first)) {
                dos.writeInt(nodeIds.get(first)); 
                dos.writeInt(nodeIds.get(second)); 
            }
        }
    }

    @FunctionalInterface
    private interface NodeSerialization {
        NodeData getData();
    }

    private static class ConstantNodeSerialization implements NodeSerialization {
        private final NodeData data;

        private ConstantNodeSerialization(NodeData data) {
            this.data = data;
        }

        public NodeData getData() {
            return this.data;
        }
    }

    private static class MultiConstantNodeSerialization implements NodeSerialization {
        private final NodeData data;
        private final List<List<Character>> characterOptions;
        private final List<List<Character>> characterChecks;

        private MultiConstantNodeSerialization(NodeData data, List<List<Character>> characterOptions, List<List<Character>> characterChecks) {
            this.data = data;
            this.characterOptions = characterOptions;
            this.characterChecks = characterChecks;
        }

        public NodeData getData() {
            return this.data;
        }
    }

    private static class AlternationNodeSerialization implements NodeSerialization {
        private final NodeData data;
        private final int first;
        private final int second;

        private AlternationNodeSerialization(NodeData data, int first, int second) {
            this.data = data;
            this.first = first;
            this.second = second;
        }

        public NodeData getData() {
            return this.data;
        }
    }

    private static class MultiAlternationNodeSerialization implements NodeSerialization {
        private final NodeData data;
        private final List<Integer> children;

        private MultiAlternationNodeSerialization(NodeData data, List<Integer> children) {
            this.data = data;
            this.children = children;
        }

        public NodeData getData() {
            return this.data;
        }
    }

    private static class RepetitionNodeSerialization implements NodeSerialization {
        private final NodeData data;
        private final int start;
        private final int rep;
        private final int end;

        private RepetitionNodeSerialization(NodeData data, int start, int rep, int end) {
            this.data = data;
            this.start = start;
            this.rep = rep;
            this.end = end;
        }

        public NodeData getData() {
            return this.data;
        }
    }

    private static class NodeDeserializer {
        private final List<NodeSerialization> nodeSerializations;
        private final List<Node> nodes;

        private NodeDeserializer(List<NodeSerialization> nodeSerializations) {
            this.nodeSerializations = nodeSerializations;
            this.nodes = new ArrayList<>();
            for (int i = 0; i < nodeSerializations.size(); i++) {
                this.nodes.add(null);
            }
        }

        private Node deserialize(int index) {
            if (this.nodes.get(index) == null) {
                NodeSerialization nodeSerialization = this.nodeSerializations.get(index);
                if (nodeSerialization instanceof ConstantNodeSerialization) {
                    this.nodes.set(index, new ConstantNode(nodeSerialization.getData()));
                } else if (nodeSerialization instanceof AlternationNodeSerialization altNodeSerialization) {
                    this.nodes.set(index, new AlternationNode(altNodeSerialization.getData(), this.deserialize(altNodeSerialization.first), this.deserialize(altNodeSerialization.second)));
                } else if (nodeSerialization instanceof MultiAlternationNodeSerialization maltNodeSerialization) {
                    List<Node> children = maltNodeSerialization.children.stream().mapToInt(childIndex -> childIndex).mapToObj(this::deserialize).collect(Collectors.toList());
                    this.nodes.set(index, new MultiAlternationNode(maltNodeSerialization.getData(), children));
                } else if (nodeSerialization instanceof RepetitionNodeSerialization repNodeSerialization) {
                    this.nodes.set(index, new RepetitionNode(repNodeSerialization.getData(), this.deserialize(repNodeSerialization.start), this.deserialize(repNodeSerialization.rep), this.deserialize(repNodeSerialization.end)));
                } else if (nodeSerialization instanceof MultiConstantNodeSerialization mconstNodeSerialization) {
                    return new MultiConstantNode(mconstNodeSerialization.getData(), mconstNodeSerialization.characterOptions, mconstNodeSerialization.characterChecks);
                } else {
                    throw new RuntimeException("Unrecognized node type: " + nodeSerialization.getClass().getName());
                }
            }
            return this.nodes.get(index);
        }

        private List<Node> deserialize() {
            for (int i = 0; i < this.nodeSerializations.size(); i++) {
                this.deserialize(i);
            }
            return this.nodes;
        }
    }

    public static Grammar deserializeNodeWithMerges(DataInputStream dis) throws IOException {
        int numNodes = dis.readInt();
        List<NodeSerialization> nodeSerializations = IntStream.range(0, numNodes).<NodeSerialization>mapToObj(i1 -> null).collect(Collectors.toCollection(() -> new ArrayList<>(numNodes)));
        for (int i = 0; i < numNodes; i++) {
            int id = dis.readInt(); 
            NodeData data = deserializeNodeData(dis); 
            int type = dis.readInt();
            switch (type) {
                case 0 -> nodeSerializations.set(id, new ConstantNodeSerialization(data));
                case 1 -> {
                    int first = dis.readInt();
                    int second = dis.readInt();
                    nodeSerializations.set(id, new AlternationNodeSerialization(data, first, second));
                }
                case 2 -> {
                    int numChildren = dis.readInt();
                    List<Integer> children = new ArrayList<>();
                    for (int j = 0; j < numChildren; j++) {
                        children.add(dis.readInt());
                    }
                    nodeSerializations.set(id, new MultiAlternationNodeSerialization(data, children));
                }
                case 3 -> {
                    int start = dis.readInt();
                    int rep = dis.readInt();
                    int end = dis.readInt();
                    nodeSerializations.set(id, new RepetitionNodeSerialization(data, start, rep, end));
                }
                case 4 -> {
                    int numCharacterOptions = dis.readInt();
                    List<List<Character>> characterOptions = new ArrayList<>();
                    List<List<Character>> characterChecks = new ArrayList<>();
                    for (int j = 0; j < numCharacterOptions; j++) {
                        int numCharacterOption = dis.readInt();
                        List<Character> characterOption = new ArrayList<>();
                        for (int k = 0; k < numCharacterOption; k++) {
                            char c = dis.readChar();
                            characterOption.add(c);
                        }
                        characterOptions.add(characterOption);
                        List<Character> characterCheck = new ArrayList<>();
                        int numCharacterCheck = dis.readInt();
                        for (int k = 0; k < numCharacterCheck; k++) {
                            char c = dis.readChar();
                            characterCheck.add(c);
                        }
                        characterChecks.add(characterCheck);
                    }
                    nodeSerializations.set(id, new MultiConstantNodeSerialization(data, characterOptions, characterChecks));
                }
                default -> throw new RuntimeException("Invalid node type: " + type);
            }
        }
        List<Node> nodes = new NodeDeserializer(nodeSerializations).deserialize();
        NodeMerges merges = new NodeMerges();
        int numMerges = dis.readInt(); 
        for (int i = 0; i < numMerges; i++) {
            int numCurMerges = dis.readInt(); 
            for (int j = 0; j < numCurMerges; j++) {
                int first = dis.readInt(); 
                int second = dis.readInt(); 
                merges.add(nodes.get(first), nodes.get(second));
            }
        }
        return new Grammar(nodes.get(0), merges);
    }
}