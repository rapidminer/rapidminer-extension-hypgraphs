package org.gesis.css.hyptrails4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;


public class TransitionsUtils {

	public static final Random r = new Random();

	/**
	 * Writes a set of transitions to a file.
	 *
	 * @param filename
	 * @param transitions
	 * @param separator
	 * @param prefix
	 * @throws IOException
	 */
	public static void writeTransitionsToFile(String filename, List<int[]> transitions, String separator, String prefix)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));

		for (int[] transition : transitions) {
			writer.write(prefix + transition[0] + separator + transition[1] + System.lineSeparator());
			writer.flush();
		}
		writer.close();
	}

	/**
	 * Prints out a list of transitions to console (just convenience)
	 */
	public static void printTransitions(List<int[]> transitions) {
		for (int[] transition : transitions) {
			System.out.println(transition[0] + " " + transition[1]);
		}
	}

	/**
	 * Converts a list of transitions to a transition matrix
	 *
	 * @param transitions
	 * @return
	 */
	public static RealMatrix computeTransitionMatrix(List<int[]> transitions) {
		int max = getMaxStateIndex(transitions);
		// +1, because 0-indexed
		return computeTransitionMatrix(transitions, max + 1);
	}

	/**
	 * Converts a list of transitions to a transition matrix with a fixed number of dimensions
	 * (e.g., to ensure equal matrix sizes if samples are drawn from transitions)
	 *
	 * @param transitions
	 * @param numberOfDimensions
	 * @return
	 */
	public static RealMatrix computeTransitionMatrix(List<int[]> transitions, int numberOfDimensions) {
		RealMatrix m = new OpenMapRealMatrix(numberOfDimensions, numberOfDimensions);
		for (int[] t : transitions) {
			m.addToEntry(t[0], t[1], 1);
		}
		return m;
	}

	private static int getMaxStateIndex(List<int[]> transitions) {
		int max = 0;
		for (int[] t : transitions) {
			max = Math.max(max, t[0]);
			max = Math.max(max, t[1]);
		}
		return max;
	}

	public static List<int[]> readFromFile(File file) throws FileNotFoundException {
		ArrayList<int[]> result = new ArrayList<int[]>();
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (!line.startsWith("#")) {
				result.add(parseLine(line));
			}
		}
		scanner.close();
		return result;
	}

	private static int[] parseLine(String line) {
		String[] tokens = line.split(" ");
		return new int[] { Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]) };
	}

	/**
	 * Picking a random sample without replacement
	 */
	public static <T> List<T> pickSample(List<T> dataset, int noSamplesStillNeeded) {
		List<T> result = new ArrayList<T>(noSamplesStillNeeded);
		// int nPicked = 0;
		int nLeft = dataset.size();
		for (T elem : dataset) {
			int rand = r.nextInt(nLeft);
			if (rand < noSamplesStillNeeded) {
				result.add(elem);
				noSamplesStillNeeded--;
			}
			nLeft--;
			if (noSamplesStillNeeded == 0) {
				break;
			}
		}
		return result;
	}
}
