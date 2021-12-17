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
public class GeneralizedFuzzyPartitionEntropyMeasure extends PreferenceMeasure.MinimumPreferenceMeasure {

	protected final MappingFunction mappingFunction;

	public GeneralizedFuzzyPartitionEntropyMeasure() {
		this.mappingFunction = new MappingFunction();
	}

	public GeneralizedFuzzyPartitionEntropyMeasure(MappingFunction mappingFunction) {
		this.mappingFunction = mappingFunction;
	}


	@Override
	public double getPreferenceValue(Dataset dataset, String attribute, String[] evidances) {

		String className = dataset.getClassName();
		List<String> terms = dataset.attr(attribute).linguisticTerms();
		List<String> classTerms = dataset.attr(className).linguisticTerms();

		double[] a = null;
		if (evidances != null) {
			for (int i = 0; i < evidances.length - 1; i += 2) {
				if (a == null) {
					a = dataset.attr(evidances[i]).fuzzyValues(evidances[i + 1]);
				} else {
					double[] aPrime = dataset.attr(evidances[i]).fuzzyValues(evidances[i + 1]);
					for (int j = 0; j < aPrime.length; j++) {
						a[j] = Math.min(a[j], aPrime[j]);
					}
				}
			}
		}


		double[] mij = new double[terms.size()];
		double[] entropies = new double[terms.size()];

		int i = 0;

		for (String term : terms) {
			double[] vals = dataset.attr(attribute).fuzzyValues(term);

			if (a != null)
				vals = Utils.min(a, vals);

			//vals = mappingFunction.map(vals);
			mij[i] = Utils.sum(mappingFunction.map(vals));

			double[] mijk = new double[classTerms.size()];
			int j = 0;
//			double[] h = new double[classTerms.size()];
			for (String ck : classTerms) {
				double[] vals2 = dataset.attr(className).fuzzyValues(ck);
				//vals2 = mappingFunction.map(vals2);
				vals2 = Utils.min(vals, vals2);

				mijk[j++] = Utils.sum(mappingFunction.map(vals2));
			}
			double mijPrime = Utils.sum(mijk);
			double hgda = Utils.ln(mappingFunction.map(mij[i]));
			//Utils.normalizeWith(mijk, Utils.sum(mijk));
			double entropy = 0;
			for (double v : mijk)
				entropy += ((v / mijPrime) * (Utils.ln(mappingFunction.map(v)) - hgda));
			entropies[i++] = -entropy;
		}
		Utils.normalizeWith(mij, Utils.sum(mij));
		double s = 0;
		for (i = 0; i < mij.length; i++)
			s += entropies[i] * mij[i];
		return s;
	}

}
