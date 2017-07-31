package com.rapidminer.extension.hypgraphs;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.tools.Ontology;

import Jama.Matrix;


public class ExampleSet2TransitionMatrix extends Operator {

	public static final String TRANSITION_NAME = "transition_attribute_name";

	private InputPort dataInput = getInputPorts().createPort("ex");

	private OutputPort transitionOutput = getOutputPorts().createPort("trans");
	private final OutputPort originalOutput = getOutputPorts().createPort("original");

	/**
	 * This Operator takes an ExampleSet and transforms two given Attributes into a transition
	 * matrix. Those Attributes need to fulfill some requirements.The first Attribute needs to be
	 * sequential ID, e.g., a time stamp or a generated ID. The second Attribute should describe a
	 * discrete state of a system, so it should be
	 *
	 * @param description
	 *
	 * @author David Arnu
	 */
	public ExampleSet2TransitionMatrix(OperatorDescription description) {

		super(description);
		getTransformer().addPassThroughRule(dataInput, originalOutput);
	}

	@Override
	public void doWork() throws OperatorException {

		ExampleSet data = dataInput.getData(ExampleSet.class);

		String transitionAttributeName = getParameter(TRANSITION_NAME);

		Attribute transitions = data.getAttributes().get(transitionAttributeName);

		if (!transitions.isNominal()) {
			throw new UserError(this, "error.119");
		}

		int matrixSize = transitions.getMapping().size();

		NominalMapping mapping = transitions.getMapping();

		Matrix matrix = new Matrix(matrixSize, matrixSize);

		for (int i = 0; i < matrixSize - 1; i++) {

			int from = mapping.getIndex(mapping.mapIndex(i));
			int to = mapping.getIndex(mapping.mapIndex(i + 1));
			matrix.set(from, to, matrix.get(from, to) + 1);
		}

		// TODO: generate new ExSet with number of rows and cols equals matrixSize

		List<Attribute> listOfAttributes = new LinkedList<>();
		Attribute attributes = AttributeFactory.createAttribute("Attributes", Ontology.STRING);
		listOfAttributes.add(attributes);
		String[] attributeNames = new String[matrixSize];

		for (int m = 0; m < matrixSize; m++) {
			listOfAttributes.add(AttributeFactory.createAttribute(mapping.mapIndex(m), Ontology.REAL));
			attributeNames[m] = mapping.mapIndex(m);
		}
		ExampleSetBuilder exampleSetBuilder = ExampleSets.from(listOfAttributes);
		exampleSetBuilder.withBlankSize(matrix.getNumberOfColumns());

		exampleSetBuilder
				.withColumnFiller(attributes, nameIndex -> attributes.getMapping().mapString(attributeNames[nameIndex]))
				.withRole(attributes, Attributes.ID_NAME);

		for (int m = 1; m <= i; m++) {
			final int index = m;
			exampleSetBuilder.withColumnFiller(listOfAttributes.get(m), lambda -> matrix.getValue(lambda, index - 1));
		}

		exampleSet = exampleSetBuilder.build();

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeAttribute(TRANSITION_NAME,
				"The name of the attribute which contains the sequential data.", dataInput, false, false));
		return types;
	}

}
