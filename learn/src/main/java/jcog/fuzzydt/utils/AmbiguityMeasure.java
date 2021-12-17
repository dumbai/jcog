/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.fuzzydt.utils;

import jcog.fuzzydt.data.Dataset;

import java.util.List;

/**
 * @author MHJ
 */
public class AmbiguityMeasure extends PreferenceMeasure.MinimumPreferenceMeasure {

	protected final double significantLevel;

	public AmbiguityMeasure(double significantLevel) {
		this.significantLevel = significantLevel;
	}

//    public double getSignificantLevel() {
//        return significantLevel;
//    }
//
//    public void setSignificantLevel(double significantLevel) {
//        this.significantLevel = significantLevel;
//    }


	private double getAmbiguity(Dataset dataset, String attrName) {

		String className = dataset.getClassName();

		List<String> terms = dataset.attr(attrName).linguisticTerms();
		List<String> classTerms = dataset.attr(className).linguisticTerms();


		double[] weights = new double[terms.size()];
		double sum = 0;

		double[][] a = new double[terms.size()][];
		int i = 0;
		for (String term : terms) {
			a[i] = dataset.attr(attrName).fuzzyValues(term);
			for (int j = 0; j < a[i].length; j++) {
				if (a[i][j] < this.significantLevel)
					a[i][j] = 0;
			}
			weights[i] = Utils.sum(a[i]);
			sum += weights[i++];
		}
		Utils.normalizeWith(weights, sum);
		double g = 0;
		for (i = 0; i < weights.length; i++) {
			double[] normalizedPossibility = new double[classTerms.size()];
			double max = -1;
			for (int j = 0; j < classTerms.size(); j++) {
				double[] b = dataset.attr(className).fuzzyValues(classTerms.get(j));
				normalizedPossibility[j] = Utils.subSetHood(a[i], b);
				if (normalizedPossibility[j] > max) {
					max = normalizedPossibility[j];
				}
			}
			Utils.normalizeWith(normalizedPossibility, max);

			g += weights[i] * Utils.ambiguity(normalizedPossibility);
		}
		return g;
	}

	// Claculate ambiguity with fuzzy evidence
	private double getAmbiguity(Dataset dataset, String[] evidence) {

		String className = dataset.getClassName();

		List<String> classTerms = dataset.attr(className).linguisticTerms();

		double[] a = null;
		for (int i = 0; i < evidence.length - 1; i += 2) {
			if (a == null) {
				a = dataset.attr(evidence[i]).fuzzyValues(evidence[i + 1]);
			} else {
				double[] aPrime = dataset.attr(evidence[i]).fuzzyValues(evidence[i + 1]);
				for (int j = 0; j < aPrime.length; j++) {
					a[j] = Math.min(a[j], aPrime[j]);
				}
			}
		}

		if (a != null) {
			for (int i = 0; i < a.length; i++) {
				if (a[i] < this.significantLevel) {
					a[i] = 0;
				}
			}
			double[] normalizedPossibility = new double[classTerms.size()];
			double max = -1;
			for (int j = 0; j < classTerms.size(); j++) {
				double[] b = dataset.attr(className).fuzzyValues(classTerms.get(j));
				normalizedPossibility[j] = Utils.subSetHood(a, b);
				if (normalizedPossibility[j] > max) {
					max = normalizedPossibility[j];
				}
			}
			Utils.normalizeWith(normalizedPossibility, max);

			return Utils.ambiguity(normalizedPossibility);
		}
		return -1;
	}

	@Override
	public double getPreferenceValue(Dataset dataset, String attribute, String[] evidances) {
		if (evidances == null) {
			return getAmbiguity(dataset, attribute);
		}

		String[] evidances1 = new String[evidances.length + 1];
		System.arraycopy(evidances, 0, evidances1, 0, evidances.length);
		evidances1[evidances.length] = dataset.getClassName();

		return getAmbiguity(dataset, evidances1);
	}
}

