package com.rapidminer.extension.hypgraphs;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewExampleSetMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.tools.Ontology;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.gesis.css.hyptrails4j.DirichletPriorGenerator;
import org.gesis.css.hyptrails4j.EvidenceComputer;
import org.gesis.css.hyptrails4j.MatrixUtils;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


public class HypGraphs extends Operator {

	public static final String PARAMETER_K = "belief factor";

	private InputPort dataInput = getInputPorts().createPort("data");
	private InputPort hypothesisInput = getInputPorts().createPort("hypothesis");

	private OutputPort evidenceOutput = getOutputPorts().createPort("evidence");
	private OutputPort priorOutput = getOutputPorts().createPort("prior");

	public HypGraphs(OperatorDescription description) {
		super(description);

		getTransformer().addRule(new GenerateNewExampleSetMDRule(evidenceOutput) {

			@Override
			public void transformMD() {
				ExampleSetMetaData md = new ExampleSetMetaData();
				md.addAttribute(new AttributeMetaData("Belief Factor", Ontology.NOMINAL));
				md.addAttribute(new AttributeMetaData("Evidence Data", Ontology.REAL));
				md.addAttribute(new AttributeMetaData("Evidence Hypothesis", Ontology.REAL));
				md.addAttribute(new AttributeMetaData("Evidence Random", Ontology.REAL));
				evidenceOutput.deliverMD(md);
			}
		});

	}

	@Override
	public void doWork() throws OperatorException {

		ExampleSet data = dataInput.getData(ExampleSet.class);
		ExampleSet hypothesis = hypothesisInput.getData(ExampleSet.class);

		int numDataRows = data.getExampleTable().size();
		int numDataCols = data.getAttributes().size();

		int numHypoRows = hypothesis.getExampleTable().size();
		int numHypoCols = hypothesis.getAttributes().size();

		// matrices have to be symmetric and of the same dimension
		if (numHypoCols != numHypoRows || numDataCols != numDataRows || numHypoCols != numDataCols) {
			throw new UserError(this, "hypgraphs.nonsymmetric");
		}

		int k = getParameterAsInt(PARAMETER_K);

		BlockRealMatrix dataMatrix = new BlockRealMatrix(numDataRows, numDataCols);
		BlockRealMatrix beliefHypothesis = new BlockRealMatrix(numHypoRows, numHypoCols);

		Iterator<Example> dataReader = data.iterator();

		int i = 0;
		while (dataReader.hasNext()) {
			Example ex = dataReader.next();
			int j = 0;

			for (Attribute att : ex.getAttributes()) {
				double dataValue = ex.getValue(att);
				if (dataValue >= 0) {
					dataMatrix.setEntry(i, j, dataValue);
				} else {
					throw new UserError(this, "hypgraphs.negativeValues", "data");
				}
				j++;
			}
			i++;
		}

		i = 0;

		Iterator<Example> hypoReader = hypothesis.iterator();
		while (hypoReader.hasNext()) {
			Example ex = hypoReader.next();
			int j = 0;

			for (Attribute att : ex.getAttributes()) {
				double dataValue = ex.getValue(att);
				if (dataValue >= 0) {
					beliefHypothesis.setEntry(i, j, dataValue);
				} else {
					throw new UserError(this, "hypgraphs.negativeValues", "hypothesis");
				}
				j++;
			}
			i++;
		}

		// build the prior matrices for the data itself and the hypothesis:
		RealMatrix priorData = DirichletPriorGenerator.computeOverallPrior(dataMatrix, k);
		checkForStop();
		RealMatrix priorHypothesis = DirichletPriorGenerator.computeOverallPrior(beliefHypothesis, k);
		checkForStop();
		// build belief and prior for uniform data (i.e. random baseline)
		RealMatrix beliefRandom = MatrixUtils.uniformMatrix(beliefHypothesis.getColumnDimension());
		RealMatrix priorRandom = DirichletPriorGenerator.computeOverallPrior(beliefRandom, k);

		// ...compute scores
		double evidenceData = EvidenceComputer.computeLogEvidence(priorData, dataMatrix);
		checkForStop();
		double evidenceHypothesis = EvidenceComputer.computeLogEvidence(priorHypothesis, dataMatrix);
		checkForStop();
		double evidenceRandom = EvidenceComputer.computeLogEvidence(priorRandom, dataMatrix);
		checkForStop();

		double[][] rawPriorHypothesis = priorHypothesis.getData();
		ExampleSet returnPriorHypothesis = ExampleSetFactory.createExampleSet(rawPriorHypothesis);

		double[][] evidences = { { k, evidenceData, evidenceHypothesis, evidenceRandom } };
		ExampleSet returnEvidence = ExampleSetFactory.createExampleSet(evidences);
		returnEvidence.getAttributes().get("att1").setName("Belief Factor");
		returnEvidence.getAttributes().get("att2").setName("Evidence Data");
		returnEvidence.getAttributes().get("att3").setName("Evidence Hypothesis");
		returnEvidence.getAttributes().get("att4").setName("Evidence Random");

		evidenceOutput.deliver(returnEvidence);
		priorOutput.deliver(returnPriorHypothesis);

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_K, "belief_factor", 1, Integer.MAX_VALUE, 1);
		type.setOptional(false);
		type.setExpert(false);
		types.add(type);
		return types;
	}

}
