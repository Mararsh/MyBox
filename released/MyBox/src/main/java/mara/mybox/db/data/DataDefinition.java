package mara.mybox.db.data;

import java.nio.charset.Charset;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
public class DataDefinition extends BaseData {

    protected long dfid;
    protected DataType dataType;
    protected String dataName, charset, delimiter;
    protected boolean hasHeader;

    public static enum DataType {
        InternalTable, DataFile, Matrix, UserTable
    }

    private void init() {
        dfid = -1;
        dataType = DataType.DataFile;
        hasHeader = false;
        charset = Charset.defaultCharset().name();
        delimiter = ",";
    }

    public DataDefinition() {
        init();
    }

    public DataDefinition(DataType dataType, String dataName) {
        init();
        this.dataType = dataType;
        this.dataName = dataName;
    }

    /*
        static methods
     */
    public static DataDefinition create() {
        return new DataDefinition();
    }

    public static boolean valid(DataDefinition data) {
        return data != null
                && data.getDataName() != null && data.getDataType() != null;
    }

    public static Object getValue(DataDefinition data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "dfid":
                return data.getDfid();
            case "data_type":
                return dataType(data.getDataType());
            case "data_name":
                return data.getDataName();
            case "charset":
                return data.getCharset();
            case "delimiter":
                return data.getDelimiter();
            case "has_header":
                return data.isHasHeader();
        }
        return null;
    }

    public static boolean setValue(DataDefinition data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dfid":
                    data.setDfid(value == null ? -1 : (long) value);
                    return true;
                case "data_type":
                    data.setDataType(dataType((short) value));
                    return true;
                case "data_name":
                    data.setDataName(value == null ? null : (String) value);
                    return true;
                case "charset":
                    data.setCharset(value == null ? null : (String) value);
                    return true;
                case "delimiter":
                    data.setDelimiter(value == null ? null : (String) value);
                    return true;
                case "has_header":
                    data.setHasHeader((boolean) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static short dataType(DataType type) {
        if (type == null) {
            return 0;
        }
        return (short) (type.ordinal());
    }

    public static DataType dataType(short type) {
        DataType[] types = DataType.values();
        if (type < 0 || type > types.length) {
            return DataType.DataFile;
        }
        return types[type];
    }

    /*
        get/set
     */
    public long getDfid() {
        return dfid;
    }

    public void setDfid(long dfid) {
        this.dfid = dfid;
    }

    public DataType getDataType() {
        return dataType;
    }

    public DataDefinition setDataType(DataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public String getDataName() {
        return dataName;
    }

    public DataDefinition setDataName(String dataName) {
        this.dataName = dataName;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public DataDefinition setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public DataDefinition setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public DataDefinition setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        return this;
    }

}
