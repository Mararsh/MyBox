package mara.mybox.data2d;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataMatrixWriter;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.IntTools;
import mara.mybox.tools.LongTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.tools.StringTools;
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
        dataType = DataType.Matrix;
        sheet = "Double";
        delimiter = MatrixDelimiter;
        charset = Charset.forName("UTF-8");
        hasHeader = false;
    }

    public DataMatrix(String type) {
        sheet = type;
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

    @Override
    public boolean defaultColNotNull() {
        return true;
    }

    @Override
    public String defaultColValue() {
        return "0";
    }

    @Override
    public ColumnDefinition.ColumnType defaultColumnType() {
        if (sheet == null) {
            sheet = "Double";
        }
        switch (sheet.toLowerCase()) {
            case "float":
                return ColumnType.Float;
            case "integer":
                return ColumnType.Integer;
            case "long":
                return ColumnType.Long;
            case "short":
                return ColumnType.Short;
            case "numberboolean":
                return ColumnType.NumberBoolean;
            case "double":
            default:
                return ColumnType.Double;
        }
    }

    @Override
    public String randomString(Random random, boolean nonNegative) {
        if (sheet == null) {
            sheet = "Double";
        }
        switch (sheet.toLowerCase()) {
            case "float":
                return NumberTools.format(FloatTools.random(random, maxRandom, nonNegative), scale);
            case "integer":
                return StringTools.format(IntTools.random(random, maxRandom, nonNegative));
            case "long":
                return StringTools.format(LongTools.random(random, maxRandom, nonNegative));
            case "short":
                return StringTools.format((short) IntTools.random(random, maxRandom, nonNegative));
            case "numberboolean":
                return random.nextInt(2) + "";
            case "double":
            default:
                return NumberTools.format(DoubleTools.random(random, maxRandom, nonNegative), scale);
        }
    }

    @Override
    public String getTypeName() {
        if (sheet == null) {
            sheet = "Double";
        }
        switch (sheet.toLowerCase()) {
            case "float":
                return message("FloatMatrix");
            case "integer":
                return message("IntegerMatrix");
            case "long":
                return message("LongMatrix");
            case "short":
                return message("ShortMatrix");
            case "numberboolean":
                return message("BooleanMatrix");
            case "double":
            default:
                return message("DoubleMatrix");
        }
    }

    @Override
    public Data2DWriter selfWriter() {
        DataMatrixWriter writer = new DataMatrixWriter();
        writer.setDataType(sheet)
                .setCharset(Charset.forName("utf-8"))
                .setDelimiter(DataMatrix.MatrixDelimiter)
                .setWriteHeader(false)
                .setTargetData(this)
                .setDataName(dataName)
                .setPrintFile(file)
                .setColumns(columns)
                .setHeaderNames(columnNames())
                .setRecordTargetFile(true)
                .setRecordTargetData(true);
        return writer;
    }

    public String toString(double d) {
        if (DoubleTools.invalidDouble(d)) {
            return Double.NaN + "";
        } else {
            return NumberTools.format(d, scale);
        }
    }

    /*
        static
     */
    public static double toDouble(String d) {
        try {
            return Double.parseDouble(d.replaceAll(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public static File file(String dataname) {
        try {
            return new File(AppPaths.getMatrixPath() + File.separator
                    + FileNameTools.filter(dataname + "_" + new Date().getTime())
                    + ".txt");
        } catch (Exception e) {
            return null;
        }
    }

    public static DataMatrix makeMatrix(String type, int colsNumber, int rowsNumber, short scale, int max) {
        try {

            String dataName = type + "_" + colsNumber + "x" + rowsNumber;
            File file = file(dataName);
            DataMatrix matrix = new DataMatrix();
            matrix.setFile(file).setSheet(type)
                    .setDataName(dataName)
                    .setScale(scale).setMaxRandom(max)
                    .setColsNumber(colsNumber)
                    .setRowsNumber(rowsNumber);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, matrix.getCharset(), false))) {
                String line;
                Random random = new Random();
                for (int i = 0; i < rowsNumber; i++) {
                    line = matrix.randomString(random, false);
                    for (int j = 1; j < colsNumber; j++) {
                        line += DataMatrix.MatrixDelimiter + matrix.randomString(random, false);
                    }
                    writer.write(line + "\n");
                }
                writer.flush();
            } catch (Exception ex) {
            }
            new TableData2DDefinition().updateData(matrix);
            return matrix;
        } catch (Exception e) {
            return null;
        }
    }

}
