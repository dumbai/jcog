/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.fuzzydt;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mohammed H. Jabreel
 */
public class TreeNode {

	private final NodeType nodeType;
	private final String title;
	private final List<TreeNode> children;
	private TreeNode parent;
	private double value;

//    public TreeNode() {
//    }
//
//    public TreeNode(NodeType nodeType, String title, TreeNode parent) {
//        this.nodeType = nodeType;
//        this.title = title;
//        this.children = new ArrayList<>();
//        this.parent = parent;
//    }

	public TreeNode(NodeType nodeType, String title) {
		this.nodeType = nodeType;
		this.title = title;
		this.children = new ArrayList<>();
		this.parent = null;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public NodeType type() {
		return nodeType;
	}

//    public TreeNode getParent() {
//        return parent;
//    }

	public String getTitle() {
		return title;
	}

//    public void setNodeType(NodeType nodeType) {
//        this.nodeType = nodeType;
//    }
//
//    public void setParent(TreeNode parent) {
//        this.parent = parent;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }

	public int getChildrenCount() {
		return this.children.size();
	}

	public void addChild(TreeNode child) {
		if (this.nodeType == NodeType.LEAF) {
			throw new IllegalArgumentException("Leaf node can not be parent ");
		}
		this.children.add(child);
		child.parent = this;
	}

//    public void addChild(String title, NodeType nodeType) {
//        this.addChild(new TreeNode(nodeType, title));
//    }
//
//    public void clearChildren() {
//        this.children.clear();
//    }
//
//    public TreeNode getChild(String title) {
//        for(TreeNode node : this.children) {
//            if(node.title.equals(title)) {
//                return node;
//            }
//        }
//        throw new IllegalArgumentException("Tree node is not exist : " + title);
//    }


	public boolean isRoot() {
		return this.parent == null;
	}

	public boolean isLeaf() {
		return this.nodeType == NodeType.LEAF;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}


}
