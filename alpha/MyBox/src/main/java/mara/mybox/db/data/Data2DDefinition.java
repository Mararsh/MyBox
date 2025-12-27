package mara.mybox.db.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.Data2DManageController;
import mara.mybox.controller.Data2DManufactureController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.MatricesManageController;
import mara.mybox.controller.MyBoxTablesController;
import mara.mybox.data.Pagination;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.db.table.BaseTableTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-2
 * @License Apache License Version 2.0
 */
public class Data2DDefinition extends BaseData {

    public long dataID;
    public DataType dataType;
    public String dataName, sheet, delimiter, comments;
    public File file;
    public Charset charset;
    public boolean hasHeader;
    public Pagination pagination;
    public long colsNumber;
    public short scale;
    public int maxRandom;
    public Date modifyTime;

    public static enum DataType {
        Texts, CSV, Excel, MyBoxClipboard, Matrix,
        DatabaseTable, InternalTable
    }

    public Data2DDefinition() {
        pagination = new Pagination(Pagination.ObjectType.Table);
        resetDefinition();
    }

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    public boolean equals(Data2DDefinition def) {
        return def != null && dataID == def.getDataID();
    }

    public Data2DDefinition cloneTo() {
        try {
            Data2DDefinition newData = new Data2DDefinition();
            newData.cloneFrom(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public Data2DDefinition cloneFrom(Data2DDefinition d) {
        try {
            if (d == null) {
                return null;
            }
            dataID = d.getDataID();
            dataType = d.getType();
            copyAllAttributes(d);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return this;
    }

    public void copyAllAttributes(Data2DDefinition d) {
        try {
            copyFileAttributes(d);
            copyDataAttributes(d);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void copyFileAttributes(Data2DDefinition d) {
        try {
            if (d == null) {
                return;
            }
            file = d.getFile();
            sheet = d.getSheet();
            modifyTime = d.getModifyTime();
            delimiter = d.getDelimiter();
            charset = d.getCharset();
            hasHeader = d.isHasHeader();
            comments = d.getComments();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void copyDataAttributes(Data2DDefinition d) {
        try {
            if (d == null) {
                return;
            }
            dataName = d.getDataName();
            scale = d.getScale();
            maxRandom = d.getMaxRandom();
            colsNumber = d.getColsNumber();
            pagination.copyFrom(d.pagination);
            comments = d.getComments();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public String info() {
        return Data2DDefinitionTools.defInfo(this);
    }

    public final void resetDefinition() {
        dataID = -1;
        file = null;
        if (!isMatrix()) {
            sheet = null;
            hasHeader = true;
            charset = null;
            delimiter = null;
        }
        dataName = null;
        colsNumber = -1;
        scale = 2;
        maxRandom = 1000;
        modifyTime = new Date();
        comments = null;
        pagination.reset();
    }

    public boolean isValidDefinition() {
        return valid(this);
    }

    public boolean hasData() {
        if (isDataFile()) {
            return FileTools.hasData(file);
        } else if (isTable()) {
            return sheet != null;
        } else {
            return true;
        }
    }

    public Data2DDefinition setCharsetName(String charsetName) {
        try {
            charset = Charset.forName(charsetName);
        } catch (Exception e) {
        }
        return this;
    }

    public String getTypeName() {
        return message(dataType.name());
    }

    public String getFileName() {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    public boolean isExcel() {
        return dataType == DataType.Excel;
    }

    public boolean isCSV() {
        return dataType == DataType.CSV;
    }

    public boolean isTexts() {
        return dataType == DataType.Texts;
    }

    public boolean isMatrix() {
        return dataType == DataType.Matrix;
    }

    public boolean isClipboard() {
        return dataType == DataType.MyBoxClipboard;
    }

    public boolean isUserTable() {
        return dataType == DataType.DatabaseTable;
    }

    public boolean isInternalTable() {
        return dataType == DataType.InternalTable
                || (dataType == DataType.DatabaseTable
                && BaseTableTools.isInternalTable(sheet));
    }

    public boolean isTable() {
        return dataType == DataType.DatabaseTable
                || dataType == DataType.InternalTable;
    }

    public boolean isDataFile() {
        return dataType == DataType.CSV
                || dataType == DataType.Excel
                || dataType == DataType.Texts
                || dataType == DataType.Matrix;
    }

    public boolean isTextFile() {
        return file != null && file.exists()
                && (dataType == DataType.CSV
                || dataType == DataType.MyBoxClipboard
                || dataType == DataType.Texts
                || dataType == DataType.Matrix);
    }

    public boolean hasComments() {
        return comments != null && !comments.isBlank();
    }

    public String getTitle() {
        String name;
        if (isDataFile() && file != null) {
            name = file.getAbsolutePath();
            if (isExcel()) {
                name += " - " + sheet;
            }
        } else if (this.isTable()) {
            name = sheet;
        } else {
            name = dataName;
        }
        if (name == null && dataID < 0) {
            name = message("NewData");
        }
        return name;
    }

    public String getName() {
        if (dataName != null && !dataName.isBlank()) {
            return dataName;
        } else {
            return shortName();
        }
    }

    public String shortName() {
        if (file != null) {
            return FileNameTools.prefix(file.getName()) + (sheet != null ? "_" + sheet : "");
        } else if (sheet != null) {
            return sheet;
        } else if (dataName != null) {
            return dataName;
        } else {
            return "";
        }
    }

    public String labelName() {
        String name = getTitle();
        name = getTypeName() + (dataID >= 0 ? " - " + dataID : "") + (name != null ? " - " + name : "");
        return name;
    }

    public String printName() {
        String title = getTitle();
        if (dataName != null && !dataName.isBlank() && !title.equals(dataName)) {
            return title + "\n" + dataName;
        } else {
            return title;
        }
    }

    public boolean validValue(String value) {
        if (value == null || dataType != DataType.Texts) {
            return true;
        }
        return !value.contains("\n") && !value.contains(delimiter);
    }

    public boolean alwayRejectInvalid() {
        return dataType == DataType.Matrix
                || dataType == DataType.DatabaseTable
                || dataType == DataType.InternalTable;
    }

    public boolean rejectInvalidWhenEdit() {
        return alwayRejectInvalid() || AppVariables.rejectInvalidValueWhenEdit;
    }

    public boolean rejectInvalidWhenSave() {
        return alwayRejectInvalid() || AppVariables.rejectInvalidValueWhenSave;
    }

    /*
        static methods
     */
    public static Data2DDefinition create() {
        return new Data2DDefinition();
    }

    public static boolean valid(Data2DDefinition data) {
        return data != null && data.getType() != null;
    }

    public static Object getValue(Data2DDefinition data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "d2did":
                return data.getDataID();
            case "data_type":
                return type(data.getType());
            case "data_name":
                return data.getDataName();
            case "file":
                return data.getFile() == null ? null : data.getFile().getAbsolutePath();
            case "sheet":
                return data.getSheet();
            case "charset":
                return data.getCharset() == null ? Charset.defaultCharset().name() : data.getCharset().name();
            case "delimiter":
                return data.getDelimiter();
            case "has_header":
                return data.isHasHeader();
            case "columns_number":
                return data.getColsNumber();
            case "rows_number":
                return data.getRowsNumber();
            case "scale":
                return data.getScale();
            case "max_random":
                return data.getMaxRandom();
            case "modify_time":
                return data.getModifyTime();
            case "comments":
                return data.getComments();
        }
        return null;
    }

    public static boolean setValue(Data2DDefinition data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "d2did":
                    data.setDataID(value == null ? -1 : (long) value);
                    return true;
                case "data_type":
                    data.setType(value == null ? DataType.Texts : type((short) value));
                    return true;
                case "data_name":
                    data.setDataName(value == null ? null : (String) value);
                    return true;
                case "file":
                    data.setFile(value == null ? null : new File((String) value));
                    return true;
                case "sheet":
                    data.setSheet(value == null ? null : (String) value);
                    return true;
                case "charset":
                    data.setCharset(value == null ? null : Charset.forName((String) value));
                    return true;
                case "delimiter":
                    data.setDelimiter(value == null ? null : (String) value);
                    return true;
                case "has_header":
                    data.setHasHeader(value == null ? false : (boolean) value);
                    return true;
                case "columns_number":
                    data.setColsNumber(value == null ? -1 : (long) value);
                    return true;
                case "rows_number":
                    data.setRowsNumber(value == null ? -1 : (long) value);
                    return true;
                case "scale":
                    data.setScale(value == null ? 2 : (short) value);
                    return true;
                case "max_random":
                    data.setMaxRandom(value == null ? 100 : (int) value);
                    return true;
                case "modify_time":
                    data.setModifyTime(value == null ? null : (Date) value);
                    return true;
                case "comments":
                    data.setComments(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString(), column + " " + value);
        }
        return false;
    }

    public static short type(DataType type) {
        if (type == null) {
            return 0;
        }
        return (short) (type.ordinal());
    }

    public static DataType type(short type) {
        DataType[] types = DataType.values();
        if (type < 0 || type > types.length) {
            return DataType.Texts;
        }
        return types[type];
    }

    public static DataType type(File file) {
        if (file == null) {
            return null;
        }
        String suffix = FileNameTools.ext(file.getAbsolutePath());
        if (suffix == null) {
            return null;
        }
        switch (suffix) {
            case "xlsx":
            case "xls":
                return DataType.Excel;
            case "csv":
                return DataType.CSV;
        }
//        if (file.getAbsolutePath().startsWith(AppPaths.getMatrixPath() + File.separator)) {
//            return DataType.Matrix;
//        }
        return DataType.Texts;
    }

    public static BaseController open(Data2DDefinition def) {
        if (def == null) {
            return Data2DManageController.open(def);
        } else {
            return open(def, def.getType());
        }
    }

    public static BaseController openType(DataType type) {
        return open(null, type);
    }

    public static BaseController open(Data2DDefinition def, DataType type) {
        if (type == null) {
            return Data2DManageController.open(def);
        }
        switch (type) {
            case CSV:
            case Excel:
            case Texts:
            case DatabaseTable:
                return Data2DManufactureController.openDef(def);
            case MyBoxClipboard:
                return DataInMyBoxClipboardController.open(def);
            case Matrix:
                return MatricesManageController.open(def);
            case InternalTable:
                return MyBoxTablesController.open(def);
            default:
                return Data2DManageController.open(def);
        }
    }


    /*
        get/set
     */
    public long getDataID() {
        return dataID;
    }

    public void setDataID(long dataID) {
        this.dataID = dataID;
    }

    public DataType getType() {
        return dataType;
    }

    public Data2DDefinition setType(DataType type) {
        this.dataType = type;
        return this;
    }

    public String getDataName() {
        return dataName;
    }

    public Data2DDefinition setDataName(String dataName) {
        this.dataName = dataName;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public Data2DDefinition setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public Data2DDefinition setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public Data2DDefinition setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Data2DDefinition setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public Data2DDefinition setPagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    public long getColsNumber() {
        return colsNumber;
    }

    public Data2DDefinition setColsNumber(long colsNumber) {
        this.colsNumber = colsNumber;
        return this;
    }

    public long getRowsNumber() {
        return pagination.rowsNumber;
    }

    public Data2DDefinition setRowsNumber(long rowsNumber) {
        pagination.setRowsNumber(rowsNumber);
        return this;
    }

    public short getScale() {
        return scale;
    }

    public Data2DDefinition setScale(short scale) {
        this.scale = scale;
        return this;
    }

    public int getMaxRandom() {
        return maxRandom;
    }

    public Data2DDefinition setMaxRandom(int maxRandom) {
        this.maxRandom = maxRandom;
        return this;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public Data2DDefinition setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
        return this;
    }

    public File getFile() {
        return file;
    }

    public Data2DDefinition setFile(File file) {
        this.file = file;
        return this;
    }

    public String getSheet() {
        return sheet;
    }

    public Data2DDefinition setSheet(String sheet) {
        this.sheet = sheet;
        return this;
    }

    public long getPagesNumber() {
        return pagination.getPagesNumber();
    }

    public void setPagesNumber(long pagesNumber) {
        pagination.setPagesNumber(pagesNumber);
    }

    public int getPageSize() {
        return pagination.getPageSize();
    }

    public void setPageSize(int pageSize) {
        pagination.setPageSize(pageSize);
    }

    public long getStartRowOfCurrentPage() {
        return pagination.getStartRowOfCurrentPage();
    }

    public void setStartRowOfCurrentPage(long startRowOfCurrentPage) {
        pagination.setStartRowOfCurrentPage(startRowOfCurrentPage);
    }

    public long getEndRowOfCurrentPage() {
        return pagination.getEndRowOfCurrentPage();
    }

    public void setEndRowOfCurrentPage(long endRowOfCurrentPage) {
        pagination.setEndRowOfCurrentPage(endRowOfCurrentPage);
    }

    public long getCurrentPage() {
        return pagination.getCurrentPage();
    }

    public void setCurrentPage(long currentPage) {
        pagination.setCurrentPage(currentPage);
    }

}
