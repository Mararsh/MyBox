package mara.mybox.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-5-18 10:37:15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleMatrixTools {

    // columnNumber is 0-based
    public static double[] columnValues(double[][] m, int columnNumber) {
        if (m == null || m.length == 0 || m[0].length <= columnNumber) {
            return null;
        }
        double[] column = new double[m.length];
        for (int i = 0; i < m.length; ++i) {
            column[i] = m[i][columnNumber];
        }
        return column;
    }

    public static double[][] columnVector(double x, double y, double z) {
        double[][] rv = new double[3][1];
        rv[0][0] = x;
        rv[1][0] = y;
        rv[2][0] = z;
        return rv;
    }

    public static double[][] columnVector(double[] v) {
        double[][] rv = new double[v.length][1];
        for (int i = 0; i < v.length; ++i) {
            rv[i][0] = v[i];
        }
        return rv;
    }

    public static double[] matrix2Array(double[][] m) {
        if (m == null || m.length == 0 || m[0].length == 0) {
            return null;
        }
        int h = m.length;
        int w = m[0].length;
        double[] a = new double[w * h];
        for (int j = 0; j < h; ++j) {
            System.arraycopy(m[j], 0, a, j * w, w);
        }
        return a;
    }

    public static double[][] array2Matrix(double[] a, int w) {
        if (a == null || a.length == 0 || w < 1) {
            return null;
        }
        int h = a.length / w;
        if (h < 1) {
            return null;
        }
        double[][] m = new double[h][w];
        for (int j = 0; j < h; ++j) {
            for (int i = 0; i < w; ++i) {
                m[j][i] = a[j * w + i];
            }
        }
        return m;
    }

    public static double[][] clone(double[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            double[][] result = new double[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                System.arraycopy(matrix[i], 0, result[i], 0, columnA);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static Number[][] clone(Number[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            Number[][] result = new Number[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                System.arraycopy(matrix[i], 0, result[i], 0, columnA);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static String print(double[][] matrix, int prefix, int scale) {
        try {
            if (matrix == null) {
                return "";
            }
            String s = "", p = "", t = "  ";
            for (int b = 0; b < prefix; b++) {
                p += " ";
            }

            int row = matrix.length, column = matrix[0].length;
            for (int i = 0; i < row; ++i) {
                s += p;
                for (int j = 0; j < column; ++j) {
                    double d = DoubleTools.scale(matrix[i][j], scale);
                    s += d + t;
                }
                s += "\n";
            }
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    public static String html(double[][] matrix, int scale) {
        try {
            if (matrix == null) {
                return "";
            }
            String s = "", t = "&nbsp;&nbsp;";
            int row = matrix.length, column = matrix[0].length;
            for (int i = 0; i < row; ++i) {
                for (int j = 0; j < column; ++j) {
                    double d = DoubleTools.scale(matrix[i][j], scale);
                    s += d + t;
                }
                s += "<BR>\n";
            }
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean same(double[][] matrixA, double[][] matrixB, int scale) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA.length != matrixB.length
                    || matrixA[0].length != matrixB[0].length) {
                return false;
            }
            int rows = matrixA.length;
            int columns = matrixA[0].length;
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < columns; ++j) {
                    if (scale < 0) {
                        if (matrixA[i][j] != matrixB[i][j]) {
                            return false;
                        }
                    } else {
                        double a = DoubleTools.scale(matrixA[i][j], scale);
                        double b = DoubleTools.scale(matrixB[i][j], scale);
                        if (a != b) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int[][] identityInt(int n) {
        try {
            if (n < 1) {
                return null;
            }
            int[][] result = new int[n][n];
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    if (i == j) {
                        result[i][j] = 1;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] identityDouble(int n) {
        try {
            if (n < 1) {
                return null;
            }
            double[][] result = new double[n][n];
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    if (i == j) {
                        result[i][j] = 1;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] randomMatrix(int n) {
        return randomMatrix(n, n);
    }

    public static double[][] randomMatrix(int m, int n) {
        try {
            if (n < 1) {
                return null;
            }
            double[][] result = new double[m][n];
            Random r = new Random();
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                    result[i][j] = r.nextDouble();
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] vertivalMerge(double[][] matrixA, double[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA[0].length != matrixB[0].length) {
                return null;
            }
            int rowsA = matrixA.length, rowsB = matrixB.length;
            int columns = matrixA[0].length;
            double[][] result = new double[rowsA + rowsB][columns];
            for (int i = 0; i < rowsA; ++i) {
                System.arraycopy(matrixA[i], 0, result[i], 0, columns);
            }
            for (int i = 0; i < rowsB; ++i) {
                System.arraycopy(matrixB[i], 0, result[i + rowsA], 0, columns);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] horizontalMerge(double[][] matrixA, double[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA.length != matrixB.length) {
                return null;
            }
            int rows = matrixA.length;
            int columnsA = matrixA[0].length, columnsB = matrixB[0].length;
            int columns = columnsA + columnsB;
            double[][] result = new double[rows][columns];
            for (int i = 0; i < rows; ++i) {
                System.arraycopy(matrixA[i], 0, result[i], 0, columnsA);
                System.arraycopy(matrixB[i], 0, result[i], columnsA, columnsB);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] add(double[][] matrixA, double[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA.length != matrixB.length
                    || matrixA[0].length != matrixB[0].length) {
                return null;
            }
            double[][] result = new double[matrixA.length][matrixA[0].length];
            for (int i = 0; i < matrixA.length; ++i) {
                double[] rowA = matrixA[i];
                double[] rowB = matrixB[i];
                for (int j = 0; j < rowA.length; ++j) {
                    result[i][j] = rowA[j] + rowB[j];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] subtract(double[][] matrixA, double[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA.length != matrixB.length
                    || matrixA[0].length != matrixB[0].length) {
                return null;
            }
            double[][] result = new double[matrixA.length][matrixA[0].length];
            for (int i = 0; i < matrixA.length; ++i) {
                double[] rowA = matrixA[i];
                double[] rowB = matrixB[i];
                for (int j = 0; j < rowA.length; ++j) {
                    result[i][j] = rowA[j] - rowB[j];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] multiply(double[][] matrixA, double[][] matrixB) {
        try {
            int rowA = matrixA.length, columnA = matrixA[0].length;
            int rowB = matrixB.length, columnB = matrixB[0].length;
            if (columnA != rowB) {
                return null;
            }
            double[][] result = new double[rowA][columnB];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnB; ++j) {
                    result[i][j] = 0;
                    for (int k = 0; k < columnA; k++) {
                        result[i][j] += matrixA[i][k] * matrixB[k][j];
                    }
                }
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static double[] multiply(double[][] matrix, double[] columnVector) {
        if (matrix == null || columnVector == null) {
            return null;
        }
        double[] outputs = new double[matrix.length];
        for (int i = 0; i < matrix.length; ++i) {
            double[] row = matrix[i];
            outputs[i] = 0d;
            for (int j = 0; j < Math.min(row.length, columnVector.length); ++j) {
                outputs[i] += row[j] * columnVector[j];
            }
        }
        return outputs;
    }

    public static double[][] transpose(double[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowsNumber = matrix.length, columnsNumber = matrix[0].length;
            double[][] result = new double[columnsNumber][rowsNumber];
            for (int row = 0; row < rowsNumber; ++row) {
                for (int col = 0; col < columnsNumber; ++col) {
                    result[col][row] = matrix[row][col];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] integer(double[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            double[][] result = new double[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[i][j] = Math.round(matrix[i][j]);
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] scale(double[][] matrix, int scale) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            double[][] result = new double[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[i][j] = DoubleTools.scale(matrix[i][j], scale);
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] multiply(double[][] matrix, double p) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            double[][] result = new double[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[i][j] = matrix[i][j] * p;
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] divide(double[][] matrix, double p) {
        try {
            if (matrix == null || p == 0) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            double[][] result = new double[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[i][j] = matrix[i][j] / p;
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] power(double[][] matrix, int n) {
        try {
            if (matrix == null || n < 0) {
                return null;
            }
            int row = matrix.length, column = matrix[0].length;
            if (row != column) {
                return null;
            }
            double[][] tmp = clone(matrix);
            double[][] result = identityDouble(row);
            while (n > 0) {
                if (n % 2 > 0) {
                    result = multiply(result, tmp);
                }
                tmp = multiply(tmp, tmp);
                n = n / 2;
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] inverseByAdjoint(double[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int row = matrix.length, column = matrix[0].length;
            if (row != column) {
                return null;
            }
            double det = determinantByComplementMinor(matrix);
            if (det == 0) {
                return null;
            }
            double[][] inverse = new double[row][column];
            double[][] adjoint = adjoint(matrix);
            for (int i = 0; i < row; ++i) {
                for (int j = 0; j < column; ++j) {
                    inverse[i][j] = adjoint[i][j] / det;
                }
            }
            return inverse;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] inverse(double[][] matrix) {
        return inverseByElimination(matrix);
    }

    public static double[][] inverseByElimination(double[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rows = matrix.length, columns = matrix[0].length;
            if (rows != columns) {
                return null;
            }
            double[][] augmented = new double[rows][columns * 2];
            for (int i = 0; i < rows; ++i) {
                System.arraycopy(matrix[i], 0, augmented[i], 0, columns);
                augmented[i][i + columns] = 1;
            }
            augmented = reducedRowEchelonForm(augmented);
            double[][] inverse = new double[rows][columns];
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < columns; ++j) {
                    inverse[i][j] = augmented[i][j + columns];
                }
            }
            return inverse;
        } catch (Exception e) {
            return null;
        }
    }

    // rowIndex, columnIndex are 0-based.
    public static double[][] complementMinor(double[][] matrix,
            int rowIndex, int columnIndex) {
        try {
            if (matrix == null || rowIndex < 0 || columnIndex < 0) {
                return null;
            }
            int rows = matrix.length, columns = matrix[0].length;
            if (rowIndex >= rows || columnIndex >= columns) {
                return null;
            }
            double[][] minor = new double[rows - 1][columns - 1];
            int minorRow = 0, minorColumn;
            for (int i = 0; i < rows; ++i) {
                if (i == rowIndex) {
                    continue;
                }
                minorColumn = 0;
                for (int j = 0; j < columns; ++j) {
                    if (j == columnIndex) {
                        continue;
                    }
                    minor[minorRow][minorColumn++] = matrix[i][j];
                }
                minorRow++;
            }
            return minor;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static double[][] adjoint(double[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int row = matrix.length, column = matrix[0].length;
            if (row != column) {
                return null;
            }
            double[][] adjoint = new double[row][column];
            if (row == 1) {
                adjoint[0][0] = matrix[0][0];
                return adjoint;
            }
            for (int i = 0; i < row; ++i) {
                for (int j = 0; j < column; ++j) {
                    adjoint[j][i] = Math.pow(-1, i + j) * determinantByComplementMinor(complementMinor(matrix, i, j));
                }
            }
            return adjoint;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static double determinant(double[][] matrix) throws Exception {
        return determinantByElimination(matrix);
    }

    public static double determinantByComplementMinor(double[][] matrix) throws
            Exception {
        try {
            if (matrix == null) {
                throw new Exception("InvalidValue");
            }
            int row = matrix.length, column = matrix[0].length;
            if (row != column) {
                throw new Exception("InvalidValue");
            }
            if (row == 1) {
                return matrix[0][0];
            }
            if (row == 2) {
                return matrix[0][0] * matrix[1][1] - matrix[1][0] * matrix[0][1];
            }
            double v = 0;
            for (int j = 0; j < column; ++j) {
                double[][] minor = complementMinor(matrix, 0, j);
                v += matrix[0][j] * Math.pow(-1, j) * determinantByComplementMinor(minor);
            }
            return v;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            throw e;
        }

    }

    public static double determinantByElimination(double[][] matrix) throws
            Exception {
        try {
            if (matrix == null) {
                throw new Exception("InvalidValue");
            }
            int rows = matrix.length, columns = matrix[0].length;
            if (rows != columns) {
                throw new Exception("InvalidValue");
            }
            double[][] ref = rowEchelonForm(matrix);
            if (ref[rows - 1][columns - 1] == 0) {
                return 0;
            }
            double det = 1;
            for (int i = 0; i < rows; ++i) {
                det *= ref[i][i];
            }
            return det;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            throw e;
        }

    }

    public static double[][] hadamardProduct(double[][] matrixA, double[][] matrixB) {
        try {
            int rowA = matrixA.length, columnA = matrixA[0].length;
            int rowB = matrixB.length, columnB = matrixB[0].length;
            if (rowA != rowB || columnA != columnB) {
                return null;
            }
            double[][] result = new double[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[i][j] = matrixA[i][j] * matrixB[i][j];
                }
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static double[][] kroneckerProduct(double[][] matrixA, double[][] matrixB) {
        try {
            int rowsA = matrixA.length, columnsA = matrixA[0].length;
            int rowsB = matrixB.length, columnsB = matrixB[0].length;
            double[][] result = new double[rowsA * rowsB][columnsA * columnsB];
            for (int i = 0; i < rowsA; ++i) {
                for (int j = 0; j < columnsA; ++j) {
                    for (int m = 0; m < rowsB; m++) {
                        for (int n = 0; n < columnsB; n++) {
                            result[i * rowsB + m][j * columnsB + n] = matrixA[i][j] * matrixB[m][n];
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static double[][] rowEchelonForm(double[][] matrix) {
        try {
            int rows = matrix.length, columns = matrix[0].length;
            double[][] result = clone(matrix);
            for (int i = 0; i < Math.min(rows, columns); ++i) {
                if (result[i][i] == 0) {
                    int row = -1;
                    for (int k = i + 1; k < rows; k++) {
                        if (result[k][i] != 0) {
                            row = k;
                            break;
                        }
                    }
                    if (row < 0) {
                        break;
                    }
                    for (int j = i; j < columns; ++j) {
                        double temp = result[row][j];
                        result[row][j] = result[i][j];
                        result[i][j] = temp;
                    }
                }
                for (int k = i + 1; k < rows; k++) {
                    if (result[i][i] == 0) {
                        continue;
                    }
                    double ratio = result[k][i] / result[i][i];
                    for (int j = i; j < columns; ++j) {
                        result[k][j] -= ratio * result[i][j];
                    }
                }
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static double[][] reducedRowEchelonForm(double[][] matrix) {
        try {
            int rows = matrix.length, columns = matrix[0].length;
            double[][] result = rowEchelonForm(matrix);
            for (int i = Math.min(rows - 1, columns - 1); i >= 0; --i) {
                double dd = result[i][i];
                if (dd == 0) {
                    continue;
                }
                for (int k = i - 1; k >= 0; k--) {
                    double ratio = result[k][i] / dd;
                    for (int j = k; j < columns; ++j) {
                        result[k][j] -= ratio * result[i][j];
                    }
                }
                for (int j = i; j < columns; ++j) {
                    result[i][j] = result[i][j] / dd;
                }
            }

            return result;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static double rank(double[][] matrix) throws Exception {
        try {
            if (matrix == null) {
                throw new Exception("InvalidValue");
            }
            int rows = matrix.length, columns = matrix[0].length;
            double[][] d = clone(matrix);
            int i = 0;
            for (i = 0; i < Math.min(rows, columns); ++i) {
                if (d[i][i] == 0) {
                    int row = -1;
                    for (int k = i + 1; k < rows; k++) {
                        if (d[k][i] != 0) {
                            row = k;
                            break;
                        }
                    }
                    if (row < 0) {
                        break;
                    }
                    for (int j = i; j < columns; ++j) {
                        double temp = d[row][j];
                        d[row][j] = d[i][j];
                        d[i][j] = temp;
                    }
                }
                for (int k = i + 1; k < rows; k++) {
                    if (d[i][i] == 0) {
                        continue;
                    }
                    double ratio = d[k][i] / d[i][i];
                    for (int j = i; j < columns; ++j) {
                        d[k][j] -= ratio * d[i][j];
                    }
                }
            }
            return i;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            throw e;
        }

    }

    public static double[][] swapRow(double matrix[][], int a, int b) {
        try {
            for (int j = 0; j < matrix[0].length; ++j) {
                double temp = matrix[a][j];
                matrix[a][j] = matrix[b][j];
                matrix[b][j] = temp;
            }
        } catch (Exception e) {
        }
        return matrix;
    }

    public static double[][] swapColumn(double matrix[][], int columnA, int columnB) {
        try {
            for (double[] matrix1 : matrix) {
                double temp = matrix1[columnA];
                matrix1[columnA] = matrix1[columnB];
                matrix1[columnB] = temp;
            }
        } catch (Exception e) {
        }
        return matrix;
    }

    public static String dataText(double[][] data, String delimiterName) {
        if (data == null || data.length == 0 || delimiterName == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        String delimiter = TextTools.delimiterValue(delimiterName);
        int rowsNumber = data.length;
        int colsNumber = data[0].length;
        for (int i = 0; i < rowsNumber; i++) {
            for (int j = 0; j < colsNumber; j++) {
                s.append(data[i][j]);
                if (j < colsNumber - 1) {
                    s.append(delimiter);
                }
            }
            s.append("\n");
        }
        return s.toString();
    }

    public static String dataHtml(double[][] data, String title) {
        if (data == null || data.length == 0) {
            return null;
        }
        int rowsNumber = data.length;
        int colsNumber = data[0].length;
        StringTable table = new StringTable(null, title == null ? Languages.message("Data") : title);
        for (int i = 0; i < rowsNumber; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < colsNumber; j++) {
                row.add(data[i][j] + "");
            }
            table.add(row);
        }
        return table.html();
    }

    public static double[][] toArray(List<List<String>> data) {
        try {
            int rowsNumber = data.size();
            int colsNumber = data.get(0).size();
            if (rowsNumber <= 0 || colsNumber <= 0) {
                return null;
            }
            double[][] array = new double[rowsNumber][colsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                List<String> row = data.get(r);
                for (int c = 0; c < row.size(); c++) {
                    double d = 0;
                    try {
                        d = Double.valueOf(row.get(c));
                    } catch (Exception e) {
                    }
                    array[r][c] = d;
                }
            }
            return array;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<List<String>> toList(double[][] array) {
        try {
            int rowsNumber = array.length;
            int colsNumber = array[0].length;
            List<List<String>> data = new ArrayList<>();
            for (int i = 0; i < rowsNumber; i++) {
                List<String> row = new ArrayList<>();
                for (int j = 0; j < colsNumber; j++) {
                    row.add(array[i][j] + "");
                }
                data.add(row);
            }
            return data;
        } catch (Exception e) {
            return null;
        }
    }

}
