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
public interface PreferenceMeasure {

	double getPreferenceValue(Dataset dataset, String attribute, String[] evidances);

//    double getPreferenceValue(Dataset dataset, String attribute);

	String getBestAttribute(Dataset dataset, String[] attributes, String[] evidances);

	String getBestAttribute(Dataset dataset);

	boolean isBetter(double v1, double v2);


	/**
	 * @author MHJ
	 */
	abstract class MaximumPreferenceMeasure implements PreferenceMeasure {

		@Override
		public boolean isBetter(double v1, double v2) {
			return v1 > v2;
		}

	}

	/**
	 * @author MHJ
	 */
	abstract class MinimumPreferenceMeasure extends PreferenceMeasureBase {


		@Override
		public boolean isBetter(double v1, double v2) {
			return v1 < v2;
		}

	}
}
