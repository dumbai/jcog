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
public class FuzzyPartitionEntropyMeasure extends PreferenceMeasure.MinimumPreferenceMeasure {

	@Override
	public double getPreferenceValue(Dataset dataset, String attribute, String[] evidances) {

		String className = dataset.getClassName();
		List<String> terms = dataset.attr(attribute).linguisticTerms();
		List<String> classTerms = dataset.attr(className).linguisticTerms();

		double[] a = null;
		if (evidances != null) {
			a = dataset.attr(evidances[0]).fuzzyValues(evidances[1]);
			for (int i = 2; i < evidances.length - 1; i += 2) {

				double[] aPrime = dataset.attr(evidances[i]).fuzzyValues(evidances[i + 1]);
				a = Utils.min(a, aPrime);

			}
		}

		double[] mi = new double[terms.size()];
		double[] entropies = new double[terms.size()];
		int j = 0;
		for (String term : terms) {
			double[] vals = dataset.attr(attribute).fuzzyValues(term);
			if (a != null) {
				vals = Utils.min(a, vals);
			}

			mi[j] = Utils.sum(vals);

			double[] mij = new double[classTerms.size()];
			int k = 0;
			for (String ck : classTerms) {
				double[] vals2 = dataset.attr(className).fuzzyValues(ck);

				vals2 = Utils.min(vals, vals2);

				mij[k++] = Utils.sum(vals2);
			}

			Utils.normalizeWith(mij, Utils.sum(mij));
			entropies[j++] = Utils.entropy(mij);
		}
		Utils.normalizeWith(mi, Utils.sum(mi));
		double s = 0;
		for (int i = 0; i < mi.length; i++) {
			s += entropies[i] * mi[i];
		}
		return s;

	}


}
