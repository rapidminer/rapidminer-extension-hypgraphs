package org.gesis.css.hyptrails4j;

import java.util.Iterator;

import org.apache.commons.math3.linear.RealMatrix;

public class DirichletPriorGenerator {

	/**
	 * Computes the informative part of the prior, i.e., the part expressing the
	 * belief. Only integer values are used for assigning "chips"
	 * 
	 * @param belief
	 *            The (not necessarily normalized) belief matrix expressing the
	 *            hypothesis
	 * @param chips
	 *            How many Chips are to be distributed in the matrix
	 * @return The informative part of the prior, i.e., the part expressing the
	 *         belief.
	 */
	public static RealMatrix computeInformativePrior(RealMatrix belief,
			int chips) {
		belief = belief
				.scalarMultiply((double) chips / MatrixUtils.sum(belief));
		RealMatrix chipsMatrix = belief.copy();
		double loss = MatrixUtils.floor(chipsMatrix);
		// this just removes numerical dirt
		int chipsRemaining = (int) Math.round(loss);
		RealMatrix remainingFractionsMatrix = belief.subtract(chipsMatrix);

		Iterator<int[]> indicesSorted = MatrixUtils.getIndicesSorted(
				remainingFractionsMatrix).iterator();
		while (chipsRemaining > 0) {
			int[] maxIndices = indicesSorted.next();
			chipsMatrix.addToEntry(maxIndices[0], maxIndices[1], 1);
			remainingFractionsMatrix.setEntry(maxIndices[0], maxIndices[1], 0);
			chipsRemaining--;
		}
		return chipsMatrix;
	}

	/**
	 * Returns the matrix of alphas for a Dirichlet distribution expressing the
	 * belief
	 * 
	 * @param belief
	 *            The (not necessarily normalized) belief matrix expressing the
	 *            hypothesis
	 * @param k
	 *            The weight of the informative part of the prior
	 * @return The matrix of alphas for a Dirichlet distribution expressing the
	 *         belief
	 */
	public static RealMatrix computeOverallPrior(RealMatrix belief, int k) {
		RealMatrix result = computeInformativePrior(belief,
				k * belief.getColumnDimension() * belief.getColumnDimension());
		result = result.scalarAdd(1);
		return result;
	}

}
