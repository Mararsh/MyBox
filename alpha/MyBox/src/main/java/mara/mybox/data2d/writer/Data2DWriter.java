package mara.mybox.data2d.writer;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseTaskController;
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

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class Data2DWriter {

    protected BaseController controller;
    protected Data2D targetData;
    protected Data2DOperate operate;
    protected File targetFile, tmpFile;
    protected ControlTargetFile targetFileController;
    protected List<String> headerNames, targetRow;
    protected List<Data2DColumn> columns;
    protected boolean writeHeader, created,
            formatValues, recordTargetFile, recordTargetData;
    protected String indent = "    ", dataName, fileSuffix;
    protected long targetRowIndex;
    protected Connection conn;

    public Data2DWriter() {
        operate = null;
        targetFileController = null;
        formatValues = false;
        writeHeader = recordTargetFile = recordTargetData = true;
    }

    final public boolean resetWriter() {
        targetData = null;
        targetRowIndex = 0;
        created = false;
        return true;
    }

    public boolean checkParameters() {
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
            targetRow = null;
            if (inRow == null || inRow.size() != columns.size()) {
                return;
            }
            targetRow = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                String v = inRow.get(i);
                if (formatValues && v != null) {
                    v = columns.get(i).format(v);
                }
                targetRow.add(v);
            }
            printTargetRow();
            targetRowIndex++;

        } catch (Exception e) {
            showError(e.toString());
        }
    }

    public void printTargetRow() {
    }

    public FxTask task() {
        if (operate != null) {
            return operate.getTask();
        } else if (controller != null) {
            return controller.getTask();
        } else {
            return null;
        }
    }

    public InvalidAs invalidAs() {
        if (operate != null) {
            return operate.getInvalidAs();
        } else {
            return InvalidAs.Blank;
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
        Data2DManufactureController.openDef(targetData);
    }

    public void showInfo(String info) {
        if (operate != null) {
            operate.showInfo(info);
        } else if (controller != null) {
            controller.displayInfo(info);
        }
    }

    public void recordFileGenerated(File file, int type) {
        if (!recordTargetFile) {
            return;
        }
        if (operate != null) {
            BaseTaskController c = operate.getTaskController();
            if (c != null) {
                c.targetFileGenerated(file, type);
            }
        } else if (controller != null) {
            controller.recordFileWritten(conn(), file, type, type);
        }
    }

    final public void showError(String error) {
        if (operate != null) {
            operate.showError(error);
        } else if (controller != null) {
            controller.displayError(error);
        } else {
            MyBoxLog.error(error);
        }
    }

    public void stop() {
        if (operate != null) {
            operate.stop();
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
    public Data2DWriter setController(BaseController controller) {
        this.controller = controller;
        return this;
    }

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

    public File getTargetFile() {
        return targetFile;
    }

    public Data2DWriter setTargetFile(File targetFile) {
        this.targetFile = targetFile;
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

    public List<String> getTargetRow() {
        return targetRow;
    }

    public Data2DWriter setTargetRow(List<String> targetRow) {
        this.targetRow = targetRow;
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

}
