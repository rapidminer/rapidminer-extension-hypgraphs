package org.gesis.css.hyptrails4j;

import java.util.HashMap;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.stat.StatUtils;


public class EvidenceComputer {

	public static HashMap<Double, Double> gammaCache = new HashMap<Double, Double>();

	/**
	 * Given a Dirichlet prior as a matrix, and the real (observed) transition matrix, computes log
	 * evidence (=marginal likelihood) of the data the prior
	 *
	 * @param prior
	 *            Matrix of a Dirichlet prior ("pseudo-observations")
	 * @param data
	 *            Matrix of observed state transitions
	 * @return
	 */
	public static double computeLogEvidence(RealMatrix prior, RealMatrix data) {
		return computeLogEvidence(prior, data, false);
	}

	/**
	 * Given a Dirichlet prior as a matrix, and the real (observed) transition matrix, computes log
	 * evidence (=marginal likelihood) of the data the prior
	 *
	 * @param prior
	 *            Matrix of a Dirichlet prior ("pseudo-observations")
	 * @param data
	 *            Matrix of observed state transitions
	 * @return
	 */
	public static double computeLogEvidence(RealMatrix prior, RealMatrix data, boolean caching) {
		double result = 0;
		for (int row = 0; row < prior.getRowDimension(); row++) {
			result += logGamma(MatrixUtils.rowSum(prior, row), caching);
			result -= logGamma(MatrixUtils.rowSum(prior, row) + MatrixUtils.rowSum(data, row), caching);

			double[] rowValuesPrior = prior.getRow(row);
			double[] rowValuesData = data.getRow(row);
			for (int col = 0; col < prior.getColumnDimension(); col++) {
				result += logGamma(rowValuesPrior[col] + rowValuesData[col], caching);
				result -= logGamma(rowValuesPrior[col], caching);
			}
		}
		return result;
	}

	private static double logGamma(double value, boolean caching) {
		if (!caching) {
			return Gamma.logGamma(value);
		}
		Double res = gammaCache.get(value);
		if (res == null) {
			res = Gamma.logGamma(value);
			gammaCache.put(value, res);
		}
		return res;
	}

	/**
	 * Computes the log evidence for a single state (experimental purposes only)
	 */
	public static double computeLogEvidence(double[] chips, double[] data) {
		double result = 0;
		result += Gamma.logGamma(StatUtils.sum(chips));
		result -= Gamma.logGamma(StatUtils.sum(chips) + StatUtils.sum(data));

		for (int i = 0; i < data.length; i++) {
			result += Gamma.logGamma(chips[i] + data[i]);
			result -= Gamma.logGamma(chips[i]);
		}

		return result;
	}

	/**
	 * Clears the global cache for the logGamma
	 */
	public static void clearCache() {
		gammaCache.clear();
	}
}
