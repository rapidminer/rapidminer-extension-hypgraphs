package com.rapidminer.extension.hypgraphs;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalAttribute;
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


public class Attribute2TransitionMatrix extends Operator {

	public static final String TRANSITION_NAME = "transition_attribute_name";

	private InputPort dataInput = getInputPorts().createPort("exa");

	private OutputPort transitionOutput = getOutputPorts().createPort("trans");
	private final OutputPort originalOutput = getOutputPorts().createPort("original");

	/**
	 * This Operator takes an ExampleSet and transforms a given Attribute into a transition
	 * matrix. The Attributes needs to fulfill some requirements. It has to be nominal and describe a
	 * discrete state of a system, so it should be ordered according to its meaning (e.g., by an time stamp ID).
	 *
	 * @param description
	 *
	 * @author David Arnu
	 */
	public Attribute2TransitionMatrix(OperatorDescription description) {

		super(description);
		getTransformer().addPassThroughRule(dataInput, originalOutput);
	}

	@Override
	public void doWork() throws OperatorException {

		ExampleSet data = dataInput.getData(ExampleSet.class);

		String transitionAttributeName = getParameter(TRANSITION_NAME);

		Attribute transitions =  data.getAttributes().get(transitionAttributeName);

		if (!transitions.isNominal()) {
			throw new UserError(this, 119);
		}

		int matrixSize = transitions.getMapping().size();
		int numberOfTransitions = data.size();

		NominalMapping mapping = transitions.getMapping();


		Matrix matrix = new Matrix(matrixSize, matrixSize);


		for (int i = 0; i < numberOfTransitions - 1; i++) {
			data.getExample(2).getNominalValue(transitions);
			int from = mapping.getIndex(data.getExample(i).getNominalValue(transitions));
			int to = mapping.getIndex(data.getExample(i+1).getNominalValue(transitions));
			matrix.set(from, to, matrix.get(from, to) + 1);
		}


		List<Attribute> listOfAttributes = new LinkedList<>();
		Attribute attributes = AttributeFactory.createAttribute("Attribute Values", Ontology.STRING);
		listOfAttributes.add(attributes);
		String[] attributeNames = new String[matrixSize];

		for (int m = 0; m < matrixSize; m++) {
			listOfAttributes.add(AttributeFactory.createAttribute(mapping.mapIndex(m), Ontology.REAL));
			attributeNames[m] = mapping.mapIndex(m);
		}
		ExampleSetBuilder exampleSetBuilder = ExampleSets.from(listOfAttributes);
		exampleSetBuilder.withBlankSize(matrix.getColumnDimension());

		exampleSetBuilder
				.withColumnFiller(attributes, nameIndex -> attributes.getMapping().mapString(attributeNames[nameIndex]))
				.withRole(attributes, Attributes.ID_NAME);

		for (int m = 1; m <= matrixSize; m++) {
			final int index = m;
			exampleSetBuilder.withColumnFiller(listOfAttributes.get(m), lambda -> matrix.get(lambda, index - 1));
		}


		originalOutput.deliver(data);
		transitionOutput.deliver(exampleSetBuilder.build());

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeAttribute(TRANSITION_NAME,
				"The name of the attribute which contains the sequential data.", dataInput, false, false));
		return types;
	}

}
