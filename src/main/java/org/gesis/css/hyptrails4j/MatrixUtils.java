package org.gesis.css.hyptrails4j;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;

public class MatrixUtils {
	/**
	 * Floors each entry of a matrix.
	 * 
	 * @param m
	 * @return The sum of all remainings by floors
	 */
	public static double floor(RealMatrix m) {
		return m.walkInColumnOrder(new RealMatrixChangingVisitor() {
			double loss = 0;

			public double visit(int row, int column, double value) {
				double newValue = Math.floor(value);
				loss += value - newValue;
				return newValue;
			}

			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) {
			}

			public double end() {
				return loss;
			}
		});
	}

	/**
	 * Computes the sum of all entries in a Matrix.
	 * 
	 * @param m
	 * @return
	 */
	public static double sum(RealMatrix m) {
		return m.walkInColumnOrder(new RealMatrixPreservingVisitor() {
			double sum;

			public void visit(int row, int column, double value) {
				sum += value;
			}

			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) {
			}

			public double end() {
				return sum;
			}
		});
	}

	/**
	 * Returns a List of all Matrix indices in DESCENDING order according to the
	 * matrix values
	 */
	public static List<int[]> getIndicesSorted(final RealMatrix m) {
		List<int[]> result = new ArrayList<int[]>();
		for (int rowIdx = 0; rowIdx < m.getRowDimension(); rowIdx++) {
			for (int colIdx = 0; colIdx < m.getColumnDimension(); colIdx++) {
				result.add(new int[] { rowIdx, colIdx });
			}
		}
		Collections.sort(result, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				double val1 = m.getEntry(o1[0], o1[1]);
				double val2 = m.getEntry(o2[0], o2[1]);
				return -Double.compare(val1, val2);
			}
		});
		return result;
	}

	/**
	 * Returns the indices (row/col) of the matrix entry with the maximum value
	 * 
	 * @param matrix
	 * @return
	 */
	public static int[] getMaximumIndices(RealMatrix matrix) {
		int row = 0;
		int col = 0;
		double maxValue = Double.NEGATIVE_INFINITY;
		for (int rowIdx = 0; rowIdx < matrix.getRowDimension(); rowIdx++) {
			for (int colIdx = 0; colIdx < matrix.getColumnDimension(); colIdx++) {
				double value = matrix.getEntry(rowIdx, colIdx);
				if (value > maxValue) {
					col = colIdx;
					row = rowIdx;
					maxValue = value;
				}
			}
		}
		return new int[] { row, col };
	}

	/**
	 * Returns the sum of all entries in this matrix row.
	 * 
	 * @param matrix
	 * @param rowIdx
	 * @return
	 */
	public static double rowSum(RealMatrix matrix, int rowIdx) {
		double result = 0;
		for (int colIdx = 0; colIdx < matrix.getColumnDimension(); colIdx++) {
			result += matrix.getEntry(rowIdx, colIdx);
		}
		return result;
	}

	/**
	 * Returns a square matrix, all entries being 1. Not to intermix with an
	 * identity matrix.
	 * 
	 * @param dimensions
	 * @return
	 */
	public static RealMatrix uniformMatrix(int dimensions) {
		OpenMapRealMatrix matrix = new OpenMapRealMatrix(dimensions, dimensions);
		for (int rowIdx = 0; rowIdx < matrix.getRowDimension(); rowIdx++) {
			for (int colIdx = 0; colIdx < matrix.getColumnDimension(); colIdx++) {
				matrix.setEntry(rowIdx, colIdx, 1);
			}
		}
		return matrix;
	}

	/**
	 * Reads a matrix from a line based file; Separator is assumed to be a
	 * single space
	 * 
	 * @param f
	 *            to be read the matrix from
	 * @return
	 * @throws IOException
	 */
	public static RealMatrix readMatrixFromFile(File f) throws IOException {
		return readMatrixFromFile(f, " ");
	}

	/**
	 * Reads a matrix from a line based file with a parameterized separator
	 * string
	 * 
	 * @param f
	 *            to be read the matrix from
	 * @return
	 * @throws IOException
	 */
	public static RealMatrix readMatrixFromFile(File f, String separator)
			throws IOException {
		int noLines = countLines(f);
		OpenMapRealMatrix m = new OpenMapRealMatrix(noLines, noLines);
		Scanner scanner = new Scanner(f);
		int row = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] tokens = line.split(separator);
			for (int column = 0; column < tokens.length; column++) {
				m.setEntry(row, column, Double.parseDouble(tokens[column]));
			}
			row++;
		}
		scanner.close();
		return m;
	}

	/**
	 * Writes a matrix to a file (line-based format, separator is a single
	 * space).
	 * 
	 * @param m
	 * @param f
	 * @throws IOException
	 */
	public static void writeMatrixToFile(RealMatrix m, File f)
			throws IOException {
		writeMatrixToFile(m, f, " ");
	}

	/**
	 * Writes a matrix to a line based file, separator can be specified.
	 * 
	 * @param m
	 * @param f
	 * @param separator
	 * @throws IOException
	 */
	public static void writeMatrixToFile(RealMatrix m, File f, String separator)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));

		for (int row = 0; row < m.getRowDimension(); row++) {
			for (int col = 0; col < m.getColumnDimension(); col++) {
				writer.write(Double.toString(m.getEntry(row, col)));
				if (col < m.getColumnDimension() - 1) {
					writer.write(separator);
				}
			}
			writer.write(System.lineSeparator());
			writer.flush();
		}
		writer.close();
	}

	/**
	 * Count the number of lines in a file. Taken from:
	 * http://stackoverflow.com/questions/453018/number-of-lines-in-a
	 * -file-in-java
	 */
	public static int countLines(File file) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean endsWithoutNewLine = false;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
				endsWithoutNewLine = (c[readChars - 1] != '\n');
			}
			if (endsWithoutNewLine) {
				++count;
			}
			return count;
		} finally {
			is.close();
		}
	}

}
