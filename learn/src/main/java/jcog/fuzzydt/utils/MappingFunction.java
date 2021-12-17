/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.fuzzydt.utils;

import java.util.Arrays;

/**
 * @author MHJ
 */
public class MappingFunction {
	public double map(double v) {
		return v;
	}

	public double[] map(double[] vals) {
		return Arrays.copyOf(vals, vals.length);
	}

	/**
	 * @author MHJ
	 */
	public static class QuadraticMappingFunction extends MappingFunction {

		@Override
		public double map(double v) {
			return v * v; //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public double[] map(double[] vals) {
			double[] mappedVals = new double[vals.length];
			for (int i = 0; i < vals.length; i++) {
				mappedVals[i] = Math.pow(vals[i], 2);
			}
			return mappedVals;
		}

	}

	/**
	 * @author Mohammed
	 */
	public static class QubicMappingFunction extends MappingFunction {
		@Override
		public double map(double v) {
			return v * v * v; //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public double[] map(double[] vals) {
			double[] mappedVals = new double[vals.length];
			for (int i = 0; i < vals.length; i++) {
				mappedVals[i] = Math.pow(vals[i], 3);
			}
			return mappedVals;
		}
	}

	/**
	 * @author Mohammed
	 */
	public static class SqrtMappingFunction extends MappingFunction {

		@Override
		public double map(double v) {
			return Math.sqrt(v);
		}

		@Override
		public double[] map(double[] vals) {
			double[] mappedVals = new double[vals.length];
			for (int i = 0; i < vals.length; i++) {
				mappedVals[i] = Math.sqrt(vals[i]);
			}
			return mappedVals;
		}


	}
}
