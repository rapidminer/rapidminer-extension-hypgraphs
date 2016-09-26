package org.gesis.css.hyptrails4j;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class DemoHypTrails {

	/**
	 * A demonstration of the HypTrails4j implementation
	 * @param args
	 */
	public static void main(String[] args) {

		// This matrix gives the empirical data, i.e., observations of state
		// transitions
		RealMatrix data = new OpenMapRealMatrix(2, 2);
		data.setRow(0, new double[] { 10, 5 });
		data.setRow(1, new double[] { 3, 5 });

		// This matrix gives you an hypothesis, i.e., the initial believe
		RealMatrix belief1 = new OpenMapRealMatrix(2, 2);
		belief1.setRow(0, new double[] { 3, 2 });
		belief1.setRow(1, new double[] { 1, 1 });
		// It is converted in a proper conjugate prior;
		// The second parameter is the k (How "strong" is the believe in our
		// hypothesis)
		RealMatrix prior1 = DirichletPriorGenerator.computeOverallPrior(
				belief1, 2);

		// In Hyptrails, you compare two hypotheses, so we need another
		// We will compare against a uniform matrix, that means all state
		// transitions are equally likely:
		RealMatrix belief2 = MatrixUtils.uniformMatrix(belief1
				.getColumnDimension());
		// Again we compute a proper prior for that believe:
		RealMatrix prior2 = DirichletPriorGenerator.computeOverallPrior(
				belief2, 2);

		// For both hypotheses we compute the evidence (to be precise, the
		// log-evidence)
		double evidence1 = EvidenceComputer.computeLogEvidence(prior1, data);
		double evidence2 = EvidenceComputer.computeLogEvidence(prior2, data);

		System.out.println(evidence1);
		System.out.println(evidence2);

		// "The bigger the evidence, the better the hypotheses"
		if (evidence1 > evidence2) {
			System.out.println("Hypothesis 1 is better!");
		} else if (evidence1 == evidence2) {
			System.out.println("Both hypotheses are equally good!");
		} else {
			System.out.println("Hypothesis 2 is better!");
		}
	}
}
