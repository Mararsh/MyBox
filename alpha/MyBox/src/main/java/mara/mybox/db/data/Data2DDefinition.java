package mara.mybox.db.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.Data2DManageController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.DataFileTextController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.DataTablesController;
import mara.mybox.controller.MatricesManageController;
import mara.mybox.controller.MyBoxTablesController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-2
 * @License Apache License Version 2.0
 */
public class Data2DDefinition extends BaseData {

    protected long d2did;
    protected Type type;
    protected String dataName, sheet, delimiter, comments;
    protected File file;
    protected Charset charset;
    protected boolean hasHeader;
    protected long colsNumber, rowsNumber;
    protected short scale;
    protected int maxRandom;
    protected Date modifyTime;

    public static enum Type {
        Texts, CSV, Excel, MyBoxClipboard, Matrix, DatabaseTable, InternalTable
    }

    public Data2DDefinition() {
        resetDefinition();
    }

    public Data2DDefinition cloneAll() {
        try {
            Data2DDefinition newData = new Data2DDefinition();
            newData.cloneAll(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public void cloneAll(Data2DDefinition d) {
        try {
            cloneBase(d);
            cloneDefinitionAttributes(d);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void cloneBase(Data2DDefinition d) {
        try {
            if (d == null) {
                return;
            }
            d2did = d.getD2did();
            type = d.getType();
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

    public void cloneDefinitionAttributes(Data2DDefinition d) {
        try {
            if (d == null) {
                return;
            }
            dataName = d.getDataName();
            scale = d.getScale();
            maxRandom = d.getMaxRandom();
            colsNumber = d.getColsNumber();
            rowsNumber = d.getRowsNumber();
            comments = d.getComments();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public final void resetDefinition() {
        d2did = -1;
        file = null;
        sheet = null;
        dataName = null;
        hasHeader = true;
        charset = null;
        delimiter = null;
        colsNumber = rowsNumber = -1;
        scale = 2;
        maxRandom = 1000;
        modifyTime = new Date();
        comments = null;
    }

    public boolean isValid() {
        return valid(this);
    }

    public boolean validData() {
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
        return message(type.name());
    }

    public String getFileName() {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    public boolean isDataFile() {
        return type == Type.CSV || type == Type.Excel || type == Type.Texts;
    }

    public boolean isExcel() {
        return type == Type.Excel;
    }

    public boolean isCSV() {
        return type == Type.CSV;
    }

    public boolean isTexts() {
        return type == Type.Texts;
    }

    public boolean isMatrix() {
        return type == Type.Matrix;
    }

    public boolean isClipboard() {
        return type == Type.MyBoxClipboard;
    }

    public boolean isTable() {
        return type == Type.DatabaseTable || type == Type.InternalTable;
    }

    public boolean isUserTable() {
        return type == Type.DatabaseTable;
    }

    public boolean isInternalTable() {
        return type == Type.InternalTable;
    }

    public String titleName() {
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
        if (name == null && d2did < 0) {
            name = message("NewData");
        }
        return name;
    }

    public String displayName() {
        String name = titleName();
        name = message(type.name()) + (d2did >= 0 ? " - " + d2did : "") + (name != null ? " - " + name : "");
        return name;
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

    public String dataName() {
        if (dataName != null && !dataName.isBlank()) {
            return dataName;
        } else {
            return shortName();
        }
    }

    public boolean validValue(String value) {
        if (value == null || type != Type.Texts) {
            return true;
        }
        return !value.contains("\n") && !value.contains(delimiter);
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
                return data.getD2did();
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
                    data.setD2did(value == null ? -1 : (long) value);
                    return true;
                case "data_type":
                    data.setType(value == null ? Type.Texts : type((short) value));
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

    public static short type(Type type) {
        if (type == null) {
            return 0;
        }
        return (short) (type.ordinal());
    }

    public static Type type(short type) {
        Type[] types = Type.values();
        if (type < 0 || type > types.length) {
            return Type.Texts;
        }
        return types[type];
    }

    public static Type type(File file) {
        if (file == null) {
            return null;
        }
        String suffix = FileNameTools.suffix(file.getAbsolutePath());
        if (suffix == null) {
            return null;
        }
        switch (suffix) {
            case "xlsx":
            case "xls":
                return Type.Excel;
            case "csv":
                return Type.CSV;
        }
        return Type.Texts;
    }

    public static BaseController open(Data2DDefinition def) {
        if (def == null) {
            return Data2DManageController.open(def);
        } else {
            return open(def, def.getType());
        }
    }

    public static BaseController openType(Type type) {
        return open(null, type);
    }

    public static BaseController open(Data2DDefinition def, Type type) {
        if (type == null) {
            return Data2DManageController.open(def);
        }
        switch (type) {
            case CSV:
                return DataFileCSVController.open(def);
            case Excel:
                return DataFileExcelController.open(def);
            case Texts:
                return DataFileTextController.open(def);
            case MyBoxClipboard:
                return DataInMyBoxClipboardController.open(def);
            case Matrix:
                return MatricesManageController.open(def);
            case DatabaseTable:
                return DataTablesController.open(def);
            case InternalTable:
                return MyBoxTablesController.open(def);
            default:
                return Data2DManageController.open(def);
        }
    }


    /*
        get/set
     */
    public long getD2did() {
        return d2did;
    }

    public void setD2did(long d2did) {
        this.d2did = d2did;
    }

    public Type getType() {
        return type;
    }

    public Data2DDefinition setType(Type type) {
        this.type = type;
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

    public long getColsNumber() {
        return colsNumber;
    }

    public Data2DDefinition setColsNumber(long colsNumber) {
        this.colsNumber = colsNumber;
        return this;
    }

    public long getRowsNumber() {
        return rowsNumber;
    }

    public Data2DDefinition setRowsNumber(long rowsNumber) {
        this.rowsNumber = rowsNumber;
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

}
