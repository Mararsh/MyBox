package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataMatrix extends DataFileText {

    public static final String MatrixDelimiter = "|";

    public DataMatrix() {
        dataType = DataType.DoubleMatrix;
        delimiter = MatrixDelimiter;
        hasHeader = false;
    }

    public int type() {
        return type(DataType.DoubleMatrix);
    }

//    public AbstractRealMatrix realMatrix(Connection conn) {
//        int rows = (int) pagination.rowsNumber;
//        int cols = (int) colsNumber;
//        if (cols > blockMatrixThreshold) {
//            return new BlockRealMatrix(rows, cols);
//        } else if (tableMatrixCell.size(conn) < rows * cols * sparseMatrixThreshold) {
//            return new OpenMapRealMatrix(rows, cols);
//        } else {
//            return new Array2DRowRealMatrix(rows, cols);
//        }
//    }
    @Override
    public String guessDelimiter() {
        return MatrixDelimiter;
    }

    @Override
    public boolean checkForLoad() {
        if (charset == null && file != null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        delimiter = MatrixDelimiter;
        hasHeader = false;
        return true;
    }

    @Override
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            dataName = message("Matrix") + " "
                    + pagination.rowsNumber + "x" + colsNumber;
        }
        return true;
    }

    public String toString(double d) {
        if (DoubleTools.invalidDouble(d)) {
            return Double.NaN + "";
        } else {
            return NumberTools.format(d, scale);
        }
    }

    public static double toDouble(String d) {
        try {
            return Double.parseDouble(d.replaceAll(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public String filename() {
        return filename(dataID);
    }

    public static String filename(long dataid) {
        try {
            return AppPaths.getMatrixPath() + File.separator + dataid + ".txt";
        } catch (Exception e) {
            return null;
        }
    }

}
