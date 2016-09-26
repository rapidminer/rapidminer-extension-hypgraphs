package org.gesis.css.hyptrails4j;

import java.io.File;
import java.io.IOException;

import org.apache.commons.math3.linear.RealMatrix;

public class DemoHypTrails2 {

	/**
	 * Another demonstration of the HypTrails4j implementation, featuring
	 * loading data and believes from files.
	 */

	public static void main(String[] args) throws IOException {

		// we can also load the transition matrix from a file
		RealMatrix data = MatrixUtils.readMatrixFromFile(new File(
				"demoData/data.dat"));

		// and the belief matrix, too
		RealMatrix belief1 = MatrixUtils.readMatrixFromFile(new File(
				"demoData/hypothesis.dat"));
		RealMatrix prior1 = DirichletPriorGenerator.computeOverallPrior(
				belief1, 2);

		// Then, the rest can be done as before:
		// ... create 2 hypothesis
		RealMatrix belief2 = MatrixUtils.uniformMatrix(belief1
				.getColumnDimension());
		RealMatrix prior2 = DirichletPriorGenerator.computeOverallPrior(
				belief2, 2);

		// ...compute scores
		double evidence1 = EvidenceComputer.computeLogEvidence(prior1, data);
		double evidence2 = EvidenceComputer.computeLogEvidence(prior2, data);

		System.out.println(evidence1);
		System.out.println(evidence2);

		// ...print out results
		if (evidence1 > evidence2) {
			System.out.println("Hypothesis 1 is better!");
		} else if (evidence1 == evidence2) {
			System.out.println("Both hypotheses are equally good!");
		} else {
			System.out.println("Hypothesis 2 is better!");
		}
	}
}
