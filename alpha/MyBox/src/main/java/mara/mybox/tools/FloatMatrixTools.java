package mara.mybox.tools;

import java.util.Random;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-5-18 10:37:15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FloatMatrixTools {

    // columnNumber is 0-based
    public static float[] columnValues(float[][] m, int columnNumber) {
        if (m == null || m.length == 0 || m[0].length <= columnNumber) {
            return null;
        }
        float[] column = new float[m.length];
        for (int i = 0; i < m.length; ++i) {
            column[i] = m[i][columnNumber];
        }
        return column;
    }

    public static float[][] columnVector(float x, float y, float z) {
        float[][] rv = new float[3][1];
        rv[0][0] = x;
        rv[1][0] = y;
        rv[2][0] = z;
        return rv;
    }

    public static float[][] columnVector(float[] v) {
        float[][] rv = new float[v.length][1];
        for (int i = 0; i < v.length; ++i) {
            rv[i][0] = v[i];
        }
        return rv;
    }

    public static float[] matrix2Array(float[][] m) {
        if (m == null || m.length == 0 || m[0].length == 0) {
            return null;
        }
        int h = m.length;
        int w = m[0].length;
        float[] a = new float[w * h];
        for (int j = 0; j < h; ++j) {
            System.arraycopy(m[j], 0, a, j * w, w);
        }
        return a;
    }

    public static float[][] array2Matrix(float[] a, int w) {
        if (a == null || a.length == 0 || w < 1) {
            return null;
        }
        int h = a.length / w;
        if (h < 1) {
            return null;
        }
        float[][] m = new float[h][w];
        for (int j = 0; j < h; ++j) {
            for (int i = 0; i < w; ++i) {
                m[j][i] = a[j * w + i];
            }
        }
        return m;
    }

    public static float[][] clone(float[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            float[][] result = new float[rowA][columnA];
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

    public static String print(float[][] matrix, int prefix, int scale) {
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
                    float d = FloatTools.scale(matrix[i][j], scale);
                    s += d + t;
                }
                s += "\n";
            }
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean same(float[][] matrixA, float[][] matrixB, int scale) {
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
                        float a = FloatTools.scale(matrixA[i][j], scale);
                        float b = FloatTools.scale(matrixB[i][j], scale);
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

    public static float[][] identityDouble(int n) {
        try {
            if (n < 1) {
                return null;
            }
            float[][] result = new float[n][n];
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

    public static float[][] randomMatrix(int n) {
        return randomMatrix(n, n);
    }

    public static float[][] randomMatrix(int m, int n) {
        try {
            if (n < 1) {
                return null;
            }
            float[][] result = new float[m][n];
            Random r = new Random();
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                    result[i][j] = r.nextFloat();
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[][] vertivalMerge(float[][] matrixA, float[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA[0].length != matrixB[0].length) {
                return null;
            }
            int rowsA = matrixA.length, rowsB = matrixB.length;
            int columns = matrixA[0].length;
            float[][] result = new float[rowsA + rowsB][columns];
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

    public static float[][] horizontalMerge(float[][] matrixA, float[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA.length != matrixB.length) {
                return null;
            }
            int rows = matrixA.length;
            int columnsA = matrixA[0].length, columnsB = matrixB[0].length;
            int columns = columnsA + columnsB;
            float[][] result = new float[rows][columns];
            for (int i = 0; i < rows; ++i) {
                System.arraycopy(matrixA[i], 0, result[i], 0, columnsA);
                System.arraycopy(matrixB[i], 0, result[i], columnsA, columnsB);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[][] add(float[][] matrixA, float[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA.length != matrixB.length
                    || matrixA[0].length != matrixB[0].length) {
                return null;
            }
            float[][] result = new float[matrixA.length][matrixA[0].length];
            for (int i = 0; i < matrixA.length; ++i) {
                float[] rowA = matrixA[i];
                float[] rowB = matrixB[i];
                for (int j = 0; j < rowA.length; ++j) {
                    result[i][j] = rowA[j] + rowB[j];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[][] subtract(float[][] matrixA, float[][] matrixB) {
        try {
            if (matrixA == null || matrixB == null
                    || matrixA.length != matrixB.length
                    || matrixA[0].length != matrixB[0].length) {
                return null;
            }
            float[][] result = new float[matrixA.length][matrixA[0].length];
            for (int i = 0; i < matrixA.length; ++i) {
                float[] rowA = matrixA[i];
                float[] rowB = matrixB[i];
                for (int j = 0; j < rowA.length; ++j) {
                    result[i][j] = rowA[j] - rowB[j];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[][] multiply(float[][] matrixA, float[][] matrixB) {
        try {
            int rowA = matrixA.length, columnA = matrixA[0].length;
            int rowB = matrixB.length, columnB = matrixB[0].length;
            if (columnA != rowB) {
                return null;
            }
            float[][] result = new float[rowA][columnB];
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

    public static float[] multiply(float[][] matrix, float[] columnVector) {
        if (matrix == null || columnVector == null) {
            return null;
        }
        float[] outputs = new float[matrix.length];
        for (int i = 0; i < matrix.length; ++i) {
            float[] row = matrix[i];
            outputs[i] = 0f;
            for (int j = 0; j < Math.min(row.length, columnVector.length); ++j) {
                outputs[i] += row[j] * columnVector[j];
            }
        }
        return outputs;
    }

    public static float[][] transpose(float[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            float[][] result = new float[columnA][rowA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[j][i] = matrix[i][j];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static Number[][] transpose(Number[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            Number[][] result = new Number[columnA][rowA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[j][i] = matrix[i][j];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[][] normalize(float[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            float[][] result = new float[rowA][columnA];
            float sum = 0;
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    sum += matrix[i][j];
                }
            }
            if (sum == 0) {
                return null;
            }
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[i][j] = matrix[i][j] / sum;
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[][] integer(float[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            float[][] result = new float[rowA][columnA];
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

    public static float[][] scale(float[][] matrix, int scale) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            float[][] result = new float[rowA][columnA];
            for (int i = 0; i < rowA; ++i) {
                for (int j = 0; j < columnA; ++j) {
                    result[i][j] = FloatTools.scale(matrix[i][j], scale);
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[][] multiply(float[][] matrix, float p) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            float[][] result = new float[rowA][columnA];
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

    public static float[][] divide(float[][] matrix, float p) {
        try {
            if (matrix == null || p == 0) {
                return null;
            }
            int rowA = matrix.length, columnA = matrix[0].length;
            float[][] result = new float[rowA][columnA];
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

    public static float[][] power(float[][] matrix, int n) {
        try {
            if (matrix == null || n < 0) {
                return null;
            }
            int row = matrix.length, column = matrix[0].length;
            if (row != column) {
                return null;
            }
            float[][] tmp = clone(matrix);
            float[][] result = identityDouble(row);
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

    public static float[][] inverseByAdjoint(float[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int row = matrix.length, column = matrix[0].length;
            if (row != column) {
                return null;
            }
            float det = determinantByComplementMinor(matrix);
            if (det == 0) {
                return null;
            }
            float[][] inverse = new float[row][column];
            float[][] adjoint = adjoint(matrix);
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

    public static float[][] inverse(float[][] matrix) {
        return inverseByElimination(matrix);
    }

    public static float[][] inverseByElimination(float[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rows = matrix.length, columns = matrix[0].length;
            if (rows != columns) {
                return null;
            }
            float[][] augmented = new float[rows][columns * 2];
            for (int i = 0; i < rows; ++i) {
                System.arraycopy(matrix[i], 0, augmented[i], 0, columns);
                augmented[i][i + columns] = 1;
            }
            augmented = reducedRowEchelonForm(augmented);
            float[][] inverse = new float[rows][columns];
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
    public static float[][] complementMinor(float[][] matrix,
            int rowIndex, int columnIndex) {
        try {
            if (matrix == null || rowIndex < 0 || columnIndex < 0) {
                return null;
            }
            int rows = matrix.length, columns = matrix[0].length;
            if (rowIndex >= rows || columnIndex >= columns) {
                return null;
            }
            float[][] minor = new float[rows - 1][columns - 1];
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

    public static float[][] adjoint(float[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int row = matrix.length, column = matrix[0].length;
            if (row != column) {
                return null;
            }
            float[][] adjoint = new float[row][column];
            if (row == 1) {
                adjoint[0][0] = matrix[0][0];
                return adjoint;
            }
            for (int i = 0; i < row; ++i) {
                for (int j = 0; j < column; ++j) {
                    adjoint[j][i] = (float) Math.pow(-1, i + j) * determinantByComplementMinor(complementMinor(matrix, i, j));
                }
            }
            return adjoint;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static float determinant(float[][] matrix) throws Exception {
        return determinantByElimination(matrix);
    }

    public static float determinantByComplementMinor(float[][] matrix) throws Exception {
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
            float v = 0;
            for (int j = 0; j < column; ++j) {
                float[][] minor = complementMinor(matrix, 0, j);
                v += matrix[0][j] * Math.pow(-1, j) * determinantByComplementMinor(minor);
            }
            return v;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            throw e;
        }

    }

    public static float determinantByElimination(float[][] matrix) throws Exception {
        try {
            if (matrix == null) {
                throw new Exception("InvalidValue");
            }
            int rows = matrix.length, columns = matrix[0].length;
            if (rows != columns) {
                throw new Exception("InvalidValue");
            }
            float[][] ref = rowEchelonForm(matrix);
            if (ref[rows - 1][columns - 1] == 0) {
                return 0;
            }
            float det = 1;
            for (int i = 0; i < rows; ++i) {
                det *= ref[i][i];
            }
            return det;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            throw e;
        }

    }

    public static float[][] hadamardProduct(float[][] matrixA, float[][] matrixB) {
        try {
            int rowA = matrixA.length, columnA = matrixA[0].length;
            int rowB = matrixB.length, columnB = matrixB[0].length;
            if (rowA != rowB || columnA != columnB) {
                return null;
            }
            float[][] result = new float[rowA][columnA];
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

    public static float[][] kroneckerProduct(float[][] matrixA, float[][] matrixB) {
        try {
            int rowsA = matrixA.length, columnsA = matrixA[0].length;
            int rowsB = matrixB.length, columnsB = matrixB[0].length;
            float[][] result = new float[rowsA * rowsB][columnsA * columnsB];
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

    public static float[][] rowEchelonForm(float[][] matrix) {
        try {
            int rows = matrix.length, columns = matrix[0].length;
            float[][] result = clone(matrix);
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
                        float temp = result[row][j];
                        result[row][j] = result[i][j];
                        result[i][j] = temp;
                    }
                }
                for (int k = i + 1; k < rows; k++) {
                    if (result[i][i] == 0) {
                        continue;
                    }
                    float ratio = result[k][i] / result[i][i];
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

    public static float[][] reducedRowEchelonForm(float[][] matrix) {
        try {
            int rows = matrix.length, columns = matrix[0].length;
            float[][] result = rowEchelonForm(matrix);
            for (int i = Math.min(rows - 1, columns - 1); i >= 0; --i) {
                float dd = result[i][i];
                if (dd == 0) {
                    continue;
                }
                for (int k = i - 1; k >= 0; k--) {
                    float ratio = result[k][i] / dd;
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

    public static float rank(float[][] matrix) throws Exception {
        try {
            if (matrix == null) {
                throw new Exception("InvalidValue");
            }
            int rows = matrix.length, columns = matrix[0].length;
            float[][] d = clone(matrix);
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
                        float temp = d[row][j];
                        d[row][j] = d[i][j];
                        d[i][j] = temp;
                    }
                }
                for (int k = i + 1; k < rows; k++) {
                    if (d[i][i] == 0) {
                        continue;
                    }
                    float ratio = d[k][i] / d[i][i];
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

    public static float[][] swapRow(float matrix[][], int a, int b) {
        try {
            for (int j = 0; j < matrix[0].length; ++j) {
                float temp = matrix[a][j];
                matrix[a][j] = matrix[b][j];
                matrix[b][j] = temp;
            }
        } catch (Exception e) {
        }
        return matrix;
    }

    public static float[][] swapColumn(float matrix[][], int columnA, int columnB) {
        try {
            for (float[] matrix1 : matrix) {
                float temp = matrix1[columnA];
                matrix1[columnA] = matrix1[columnB];
                matrix1[columnB] = temp;
            }
        } catch (Exception e) {
        }
        return matrix;
    }

}
