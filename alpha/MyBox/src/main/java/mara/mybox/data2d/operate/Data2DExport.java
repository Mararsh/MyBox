package mara.mybox.data2d.operate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlTargetFile;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DExport extends Data2DOperate {

    protected ControlTargetFile pathController;
    protected List<Data2DColumn> columns;
    protected List<String> columnNames;
    protected TargetType format;
    protected String indent = "    ", dataName, filePrefix;
    protected int maxLines, fileIndex, fileRowIndex;

    public Data2DExport() {
        resetExport();
    }

    public static Data2DExport create(Data2D_Edit data) {
        Data2DExport op = new Data2DExport();
        return op.setSourceData(data) ? op : null;
    }

    final public void resetExport() {
        maxLines = -1;
        format = null;
        columns = null;
        columnNames = null;
        filePrefix = null;
        fileIndex = 1;
        fileRowIndex = 0;
    }

    @Override
    public boolean checkParameters() {
        return cols != null && !cols.isEmpty()
                && writers != null && !writers.isEmpty()
                && super.checkParameters();
    }

    @Override
    public void setTargetFile(Data2DWriter writer) {
        if (writer == null) {
            return;
        }
        if (targetFile != null) {
            writer.setPrintFile(targetFile);
        } else if (pathController != null) {
            String name = filePrefix + (maxLines > 0 ? fileIndex : "");
            writer.setPrintFile(pathController.makeTargetFile(name,
                    "." + writer.getFileSuffix(), targetPath));
        }
    }

    public void checkFileSplit() {
        if (maxLines > 0 && fileRowIndex >= maxLines) {
            closeWriters();
            fileIndex++;
            fileRowIndex = 0;
            openWriters();
        }
    }

    @Override
    public boolean handleRow() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            checkFileSplit();
            targetRow = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    String value = sourceRow.get(col);
                    targetRow.add(value);
                } else {
                    targetRow.add(null);
                }
            }
            if (targetRow.isEmpty()) {
                return false;
            }
            fileRowIndex++;
            if (includeRowNumber) {
                targetRow.add(0, sourceRowIndex + "");
            }
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    /*
        control by external method
     */
    public boolean setColumns(ControlTargetFile controller, List<Data2DColumn> cols, String prefix) {
        pathController = controller;
        targetFile = null;
        columns = cols;
        columnNames = Data2DColumnTools.toNames(cols);
        return initExport(prefix);
    }

    public boolean setNames(ControlTargetFile controller, List<String> cols, String prefix) {
        pathController = controller;
        targetFile = null;
        columnNames = cols;
        columns = Data2DColumnTools.toColumns(columnNames);
        return initExport(prefix);
    }

    public boolean setNames(ControlTargetFile controller, List<String> cols) {
        pathController = controller;
        columnNames = cols;
        columns = Data2DColumnTools.toColumns(columnNames);
        return initExport(null);
    }

    private boolean initExport(String prefix) {
        if (pathController == null || writers == null || writers.isEmpty()
                || columns == null || columns.isEmpty()) {
            return false;
        }
        filePrefix = prefix != null ? prefix : "mexport";
        targetFile = null;
        File file = pathController.pickFile();
        if (file == null) {
            targetPath = new File(AppPaths.getGeneratedPath() + File.separator);
        } else if (file.isDirectory()) {
            targetPath = file;
        } else {
            targetPath = file.getParentFile();
        }
        if (!pathController.isIsDirectory()) {
            if (file != null) {
                if (!file.exists()) {
                    targetFile = file;
                } else {
                    filePrefix = FileNameTools.prefix(file.getName());
                }
            }
        }
        if (includeRowNumber) {
            if (columns != null) {
                columns.add(0, new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.Long));
            }
            if (columnNames != null) {
                columnNames.add(0, message("RowNumber"));
            }
        }
        for (Data2DWriter writer : writers) {
            writer.setColumns(columns).setHeaderNames(columnNames);
        }
        sourceRowIndex = 0;
        fileIndex = 1;
        fileRowIndex = 0;
        return true;
    }

    public void writeRow(List<String> inRow) {
        try {
            if (inRow == null) {
                return;
            }
            checkFileSplit();
            targetRow = new ArrayList<>();
            for (int i = 0; i < inRow.size(); i++) {
                String v = inRow.get(i);
                targetRow.add(v);
            }
            fileRowIndex++;
            sourceRowIndex++;
            if (includeRowNumber) {
                targetRow.add(0, sourceRowIndex + "");
            }
            writeRow();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        set / get
     */
    public void setPathController(ControlTargetFile pathController) {
        this.pathController = pathController;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Data2DColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<Data2DColumn> columns) {
        this.columns = columns;
    }

    public String getIndent() {
        return indent;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public int getMaxLines() {
        return maxLines;
    }

    public Data2DExport setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        return this;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

}
