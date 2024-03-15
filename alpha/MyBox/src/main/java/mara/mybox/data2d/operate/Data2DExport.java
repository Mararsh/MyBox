package mara.mybox.data2d.operate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlTargetFile;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DExport extends Data2DOperate {

    protected ControlTargetFile targetFileController;
    protected List<String> names;
    protected List<Data2DColumn> columns;
    protected boolean firstRow, skip, created;
    protected TargetType format;
    protected String indent = "    ", dataName, filePrefix;
    protected int maxLines, fileIndex, fileRowIndex, dataRowIndex;

    public Data2DExport() {
        firstRow = true;
        formatValues = false;
        maxLines = -1;
        format = null;
        names = null;
        created = false;
        filePrefix = null;
        fileIndex = 1;
        fileRowIndex = dataRowIndex = 0;
    }

    public static Data2DExport create(Data2D_Edit data) {
        Data2DExport op = new Data2DExport();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return super.checkParameters() && cols != null && !cols.isEmpty();
    }

    public boolean initParameters() {
        try {
            format = null;
            names = null;
            initWriters();
            initFiles();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean initWriters() {
        firstRow = true;
        created = false;
        return true;
    }

    public boolean initFiles() {
        try {
            filePrefix = null;
            fileIndex = 1;
            fileRowIndex = dataRowIndex = 0;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRow() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            if (maxLines > 0 && fileRowIndex >= maxLines) {
                closeWriters();
                fileIndex++;
                fileRowIndex = 0;
                openWriters();
            }
            targetRow = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    String value = sourceRow.get(col);
                    if (value != null && formatValues) {
                        value = sourceData.column(col).format(value);
                    }
                    targetRow.add(value);
                } else {
                    targetRow.add(null);
                }
            }
            if (targetRow.isEmpty()) {
                return false;
            }
            dataRowIndex++;
            if (includeRowNumber) {
                targetRow.add(0, sourceRowIndex + "");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
        control by external method
     */
    public boolean initPath(ControlTargetFile controller, List<Data2DColumn> cols, String prefix) {
        targetFileController = controller;
        targetFile = null;
        columns = cols;
        names = Data2DTools.toNames(cols);
        return setValues(prefix);
    }

    public boolean initFiles(ControlTargetFile controller, List<String> cols, String prefix) {
        targetFileController = controller;
        targetFile = null;
        names = cols;
        columns = Data2DTools.toColumns(names);
        return setValues(prefix) && openWriters();
    }

    public boolean initFile(ControlTargetFile controller, List<Data2DColumn> cols, String prefix) {
        targetFileController = controller;
        columns = cols;
        names = Data2DTools.toNames(cols);
        return setValues(prefix);
    }

    public boolean initFiles(ControlTargetFile controller, List<String> cols) {
        targetFileController = controller;
        names = cols;
        columns = Data2DTools.toColumns(names);
        return true;
    }

    public boolean setValues(String prefix) {
        initFiles();
        filePrefix = prefix != null ? prefix : "mexport";
        targetFile = null;
        File file = targetFileController.file();
        if (file == null) {
            targetPath = new File(AppPaths.getGeneratedPath() + File.separator);
        } else if (file.isDirectory()) {
            targetPath = file;
        } else {
            targetPath = file.getParentFile();
        }
        if (!targetFileController.isIsDirectory()) {
            if (file != null) {
                if (!file.exists()) {
                    targetFile = file;
                } else {
                    filePrefix = FileNameTools.prefix(file.getName());
                }
            }
        }
        skip = targetFileController.isSkip();
        if (includeRowNumber) {
            if (columns != null) {
                columns.add(0, new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            }
            if (names != null) {
                names.add(0, message("RowNumber"));
            }
        }
        return true;
    }

    public File makeTargetFile(String prefix, String suffix) {
        if (targetFile != null) {
            return targetFile;
        } else {
            return targetFileController.makeTargetFile(prefix, "." + suffix, targetPath);
        }
    }

    public void writeRow(List<String> inRow) {
        try {
            if (inRow == null) {
                return;
            }
            if (maxLines > 0 && fileRowIndex >= maxLines) {
                closeWriters();
                fileIndex++;
                fileRowIndex = 0;
                Data2DExport.this.openWriters();
            }
            dataRowIndex++;
            if (includeRowNumber) {
                inRow.add(0, dataRowIndex + "");
            }
            targetRow = inRow;
            if (formatValues) {
                targetRow = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    String v = inRow.get(i);
                    if (v != null) {
                        v = columns.get(i).format(v);
                    }
                    targetRow.add(v);
                }
            }
            writeRow();
            fileRowIndex++;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        set / get
     */
    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<Data2DColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<Data2DColumn> columns) {
        this.columns = columns;
    }

    public boolean isFirstRow() {
        return firstRow;
    }

    public void setFirstRow(boolean firstRow) {
        this.firstRow = firstRow;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
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

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
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

    public boolean isCreated() {
        return created;
    }

}
