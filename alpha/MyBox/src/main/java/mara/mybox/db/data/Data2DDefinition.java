package mara.mybox.db.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-11-2
 * @License Apache License Version 2.0
 */
public class Data2DDefinition extends BaseData {

    protected long d2did;
    protected Type type;
    protected String dataName, delimiter, comments;
    protected File file;
    protected Charset charset;
    protected boolean hasHeader;
    protected long colsNumber, rowsNumber;
    protected short scale;
    protected int maxRandom;
    protected Date modifyTime;

    public static enum Type {
        Text, CSV, Excel, Clipboard, Matrix, Table
    }

    public Data2DDefinition() {
        resetDefinition();
    }

    public void cloneAll(Data2DDefinition def) {
        try {
            if (def == null) {
                return;
            }
            d2did = def.getD2did();
            type = def.getType();
            dataName = def.getDataName();
            delimiter = def.getDelimiter();
            file = def.getFile();
            charset = def.getCharset();
            hasHeader = def.isHasHeader();
            colsNumber = def.getColsNumber();
            rowsNumber = def.getRowsNumber();
            scale = def.getScale();
            maxRandom = def.getMaxRandom();
            modifyTime = def.getModifyTime();
            comments = def.getComments();
        } catch (Exception e) {
        }
    }

    public Data2DDefinition cloneAll() {
        try {
            Data2DDefinition newData = new Data2DDefinition();
            newData.cloneAll(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public final void resetDefinition() {
        d2did = -1;
        file = null;
        dataName = null;
        hasHeader = true;
        charset = null;
        delimiter = null;
        colsNumber = rowsNumber = 0;
        scale = 2;
        maxRandom = 1000;
        modifyTime = new Date();
        comments = null;
    }

    public boolean isValid() {
        return valid(this);
    }

    public Data2DDefinition setCharsetName(String charsetName) {
        try {
            charset = Charset.forName(charsetName);
        } catch (Exception e) {
        }
        return this;
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
                    data.setType(type((short) value));
                    return true;
                case "data_name":
                    data.setDataName(value == null ? null : (String) value);
                    return true;
                case "file":
                    data.setFile(value == null ? null : new File((String) value));
                    return true;
                case "charset":
                    data.setCharset(value == null ? null : Charset.forName((String) value));
                    return true;
                case "delimiter":
                    data.setDelimiter(value == null ? null : (String) value);
                    return true;
                case "has_header":
                    data.setHasHeader((boolean) value);
                    return true;
                case "columns_number":
                    data.setColsNumber(value == null ? 3 : (long) value);
                    return true;
                case "rows_number":
                    data.setRowsNumber(value == null ? 3 : (long) value);
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
            MyBoxLog.debug(e.toString());
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
            return Type.Text;
        }
        return types[type];
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

}
