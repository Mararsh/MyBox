package mara.mybox.data2d.writer;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlTargetFile;
import mara.mybox.controller.Data2DManufactureController;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Append;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.DatabaseTable;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.HTML;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Insert;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.JSON;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Matrix;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.MyBoxClipboard;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.PDF;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Replace;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.SystemClipboard;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.XML;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class Data2DWriter {

    protected Data2D targetData;
    protected Data2DOperate operate;
    protected File printFile, tmpFile;
    protected ControlTargetFile targetFileController;
    protected List<String> headerNames, printRow;
    protected List<Data2DColumn> columns;
    public boolean writeHeader, created, validateValue,
            formatValues, recordTargetFile, recordTargetData;
    protected String indent = "    ", dataName, fileSuffix, value;
    protected long targetRowIndex;
    protected Connection conn;
    protected int rowSize;
    public InvalidAs invalidAs;

    public Data2DWriter() {
        operate = null;
        targetFileController = null;
        formatValues = false;
        validateValue = false;
        writeHeader = recordTargetFile = recordTargetData = true;
        invalidAs = null;
    }

    public boolean checkParameters() {
        if (operate != null) {
            if (invalidAs == null) {
                invalidAs = operate.getInvalidAs();
            }
        }
        validateValue = AppVariables.rejectInvalidValueWhenSave;
        return true;
    }

    public boolean resetWriter() {
        targetData = null;
        targetRowIndex = 0;
        created = false;
        return true;
    }

    public boolean openWriter() {
        if (!checkParameters()) {
            return false;
        }
        resetWriter();
        return true;
    }

    public void writeRow(List<String> inRow) {
        try {
            printRow = null;
            if (inRow == null || inRow.isEmpty()) {
                return;
            }
            printRow = new ArrayList<>();
            rowSize = inRow.size();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                value = i < rowSize ? inRow.get(i) : null;
                if ((validateValue || invalidAs == InvalidAs.Fail)
                        && !column.validValue(value)) {
                    failStop(message("InvalidData") + ". "
                            + message("Column") + ":" + column.getColumnName() + "  "
                            + message("Value") + ": " + value);
                    return;
                }
                if (formatValues) {
                    value = column.format(value, invalidAs);
                } else {
                    value = column.trimValue(value, invalidAs);
                }
                printRow.add(value);
            }
            printTargetRow();
            targetRowIndex++;
            if (targetRowIndex % 100 == 0) {
                showInfo(message("Written") + ": " + targetRowIndex);
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    public void printTargetRow() {
    }

    public FxTask task() {
        if (operate != null) {
            return operate.getTask();
        } else {
            return null;
        }
    }

    public long sourceRowIndex() {
        if (operate != null) {
            return operate.getSourceRowIndex();
        } else {
            return -1;
        }
    }

    public void closeWriter() {
        created = false;
    }


    /*
        value/status
     */
    public Connection conn() {
        if (operate != null) {
            conn = operate.conn();
        } else {
            conn = DerbyBase.getConnection();
        }
        return conn;
    }

    public Data2D sourceData() {
        if (operate != null) {
            return operate.getSourceData();
        } else {
            return null;
        }
    }

    public void showResult() {
        if (targetData == null) {
            return;
        }
        Data2DManufactureController c = Data2DManufactureController.openDef(targetData);
        c.setAlwaysOnTop();
    }

    public void showInfo(String info) {
        if (operate != null) {
            operate.showInfo(info);
        }
    }

    public void recordFileGenerated(File file, int type) {
        if (file == null || !file.exists()) {
            return;
        }
        showInfo(message("Generated") + ": " + file + "  "
                + message("FileSize") + ": " + file.length());
        showInfo(message("RowsNumber") + ": " + targetRowIndex);
        if (!recordTargetFile || operate == null) {
            return;
        }
        BaseController c = operate.getController();
        if (c != null) {
            c.recordFileWritten(conn(), file, type, type);
        }
        operate.addPrintedFile(file);
    }

    final public void showError(String error) {
        if (operate != null) {
            operate.showError(error);
        } else {
            MyBoxLog.error(error);
        }
    }

    public void stop() {
        if (operate != null) {
            operate.stop();
        }
    }

    public void failStop(String error) {
        if (operate != null) {
            operate.failStop(error);
        }
    }

    public void setFailed() {
        if (operate != null) {
            operate.setFailed();
        }
    }

    public boolean isFailed() {
        return operate != null && operate.isFailed();
    }

    public boolean isStopped() {
        return operate != null && operate.isStopped();
    }

    /*
        static
     */
    public static Data2DWriter getWriter(TargetType targetType) {
        try {
            if (targetType == null) {
                return null;
            }
            Data2DWriter writer = null;
            switch (targetType) {
                case CSV:
                    writer = new DataFileCSVWriter();
                    break;
                case Excel:
                    writer = new DataFileExcelWriter();
                    break;
                case Text:
                    writer = new DataFileTextWriter();
                    break;
                case DatabaseTable:
                    writer = new DataTableWriter();
                    break;
                case Matrix:
                    writer = new MatrixWriter();
                    break;
                case MyBoxClipboard:
                    writer = new MyBoxClipboardWriter();
                    break;
                case SystemClipboard:
                    writer = new SystemClipboardWriter();
                    break;
                case HTML:
                    writer = new HtmlWriter();
                    break;
                case PDF:
                    writer = new PdfWriter();
                    break;
                case JSON:
                    writer = new JsonWriter();
                    break;
                case XML:
                    writer = new XmlWriter();
                    break;
                case Replace:
                case Insert:
                case Append:
                    writer = new SystemClipboardWriter();
                    break;
            }
            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        get/set
     */
    public Data2D getTargetData() {
        return targetData;
    }

    public Data2DWriter setTargetData(Data2D targetData) {
        this.targetData = targetData;
        return this;
    }

    public Data2DOperate getOperate() {
        return operate;
    }

    public Data2DWriter setOperate(Data2DOperate operate) {
        this.operate = operate;
        return this;
    }

    public File getPrintFile() {
        return printFile;
    }

    public Data2DWriter setPrintFile(File printFile) {
        this.printFile = printFile;
        return this;
    }

    public ControlTargetFile getTargetFileController() {
        return targetFileController;
    }

    public Data2DWriter setTargetFileController(ControlTargetFile targetFileController) {
        this.targetFileController = targetFileController;
        return this;
    }

    public List<String> getHeaderNames() {
        return headerNames;
    }

    public Data2DWriter setHeaderNames(List<String> headerNames) {
        this.headerNames = headerNames;
        return this;
    }

    public List<String> getPrintRow() {
        return printRow;
    }

    public Data2DWriter setTargetRow(List<String> targetRow) {
        this.printRow = targetRow;
        return this;
    }

    public List<Data2DColumn> getColumns() {
        return columns;
    }

    public Data2DWriter setColumns(List<Data2DColumn> columns) {
        this.columns = columns;
        return this;
    }

    public boolean isWriteHeader() {
        return writeHeader;
    }

    public Data2DWriter setWriteHeader(boolean writeHeader) {
        this.writeHeader = writeHeader;
        return this;
    }

    public boolean isCreated() {
        return created;
    }

    public Data2DWriter setCreated(boolean created) {
        this.created = created;
        return this;
    }

    public boolean isFormatValues() {
        return formatValues;
    }

    public Data2DWriter setFormatValues(boolean formatValues) {
        this.formatValues = formatValues;
        return this;
    }

    public boolean isValidateValue() {
        return validateValue;
    }

    public Data2DWriter setValidateValue(boolean validateValue) {
        this.validateValue = validateValue;
        return this;
    }

    public String getIndent() {
        return indent;
    }

    public Data2DWriter setIndent(String indent) {
        this.indent = indent;
        return this;
    }

    public String getDataName() {
        return dataName;
    }

    public Data2DWriter setDataName(String dataName) {
        this.dataName = dataName;
        return this;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public Data2DWriter setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
        return this;
    }

    public boolean isRecordTargetFile() {
        return recordTargetFile;
    }

    public Data2DWriter setRecordTargetFile(boolean recordTargetFile) {
        this.recordTargetFile = recordTargetFile;
        return this;
    }

    public boolean isRecordTargetData() {
        return recordTargetData;
    }

    public Data2DWriter setRecordTargetData(boolean recordTargetData) {
        this.recordTargetData = recordTargetData;
        return this;
    }

    public long getTargetRowIndex() {
        return targetRowIndex;
    }

    public Data2DWriter setTargetRowIndex(long targetRowIndex) {
        this.targetRowIndex = targetRowIndex;
        return this;
    }

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

    public void setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
    }

}
