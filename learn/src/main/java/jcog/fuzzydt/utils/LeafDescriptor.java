/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.fuzzydt.utils;

/**
 * @author MHJ
 */
public class LeafDescriptor {

	private final boolean leaf;

	private final double degreeOfTruth;

//    public LeafDescriptor() {
//    }

	public LeafDescriptor(boolean leaf, double degreeOfTruth) {
		this.leaf = leaf;
		this.degreeOfTruth = degreeOfTruth;
	}

	public double truth() {
		return degreeOfTruth;
	}

//    public void setDegreeOfTruth(double degreeOfTruth) {
//        this.degreeOfTruth = degreeOfTruth;
//    }

	public boolean isLeaf() {
		return leaf;
	}

//    public void setLeaf(boolean leaf) {
//        this.leaf = leaf;
//    }
//


}
