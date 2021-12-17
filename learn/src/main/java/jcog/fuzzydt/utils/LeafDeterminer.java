/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.fuzzydt.utils;

import jcog.fuzzydt.data.Dataset;

/**
 * @author MHJ
 */
@FunctionalInterface
public interface LeafDeterminer {
	LeafDescriptor leafDescriptor(Dataset dataset, String[] evidances);
}
