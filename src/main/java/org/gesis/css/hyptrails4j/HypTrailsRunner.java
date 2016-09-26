package org.gesis.css.hyptrails4j;

import java.io.File;
import java.io.IOException;

import org.apache.commons.math3.linear.RealMatrix;


/**
 * HypTrailsRunner: based on DemoHypTrails & DemoHypTrails2 by Florian Lemmerich
 *
 * Copyright (C) 2015 by Martin Atzmueller.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 *
 * @author Martin Atzmueller
 * @version 20150610
 *
 */
public class HypTrailsRunner {

	public static void main(String[] args) throws IOException {
		// OptionParser op = new OptionParser("i:h:k:q");
		// OptionSet ops = op.parse(args);

		// if (!ops.has("i")) {
		// throw new IllegalStateException("Need input file -i");
		// }
		// if (!ops.has("h")) {
		// throw new IllegalStateException("Need hypothesis file -h");
		// }

		// String dataFileName = (String) ops.valueOf("i");
		// String hypothesisFilename = (String) ops.valueOf("h");
		String dataFileName = "/home/darnu/Dokumente/FEE/Dokumente/HyperGraph Demo/matrix_normal_demo.csv";
		String hypothesisFilename = "/home/darnu/Dokumente/FEE/Dokumente/HyperGraph Demo/matrix_anomaly_demo.csv";

		// Parameter k: How "strong" is the believe in our hypothesis)
		int k = 2;
		// if (!ops.has("k")) {
		// k = 2;
		// } else {
		// k = Integer.valueOf((String) ops.valueOf("k"));
		// }

		final boolean quiet = false;
		// if (ops.has("q")) {
		// quiet = true;
		// } else {
		// quiet = false;
		// }

		// we load the transition matrix from a file
		RealMatrix data = MatrixUtils.readMatrixFromFile(new File(dataFileName));

		// and the belief matrix, too
		RealMatrix belief1 = MatrixUtils.readMatrixFromFile(new File(hypothesisFilename));
		RealMatrix prior1 = DirichletPriorGenerator.computeOverallPrior(belief1, k);

		// In Hyptrails, you compare two hypotheses:
		// As a baseline, we will compare against a uniform matrix, that means
		// all state transitions are equally likely:
		RealMatrix belief2 = MatrixUtils.uniformMatrix(belief1.getColumnDimension());
		RealMatrix prior2 = DirichletPriorGenerator.computeOverallPrior(belief2, k);

		// ...compute scores
		double evidence1 = EvidenceComputer.computeLogEvidence(prior1, data);
		double evidence2 = EvidenceComputer.computeLogEvidence(prior2, data);

		System.out.println(evidence1 + "\t" + evidence2);

		if (!quiet) {
			// ...print out the results
			if (evidence1 > evidence2) {
				System.out.println("Provided hypothesis is better! (" + hypothesisFilename + ")");
			} else if (evidence1 == evidence2) {
				System.out.println("Both hypotheses are equally good!");
			} else {
				System.out.println("Baseline (Uniform) Hypothesis is better!");
			}
		}
	}

}
