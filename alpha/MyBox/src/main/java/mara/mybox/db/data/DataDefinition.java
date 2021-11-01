package mara.mybox.db.data;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
public class DataDefinition extends BaseData {

    protected long dfid;
    protected DataType dataType;
    protected String dataName, charset, delimiter, comments;
    protected File file;
    protected boolean hasHeader;
    protected long colsNumber, rowsNumber;
    protected short scale;
    protected int maxRandom;
    protected Date modifyTime;

    public static enum DataType {
        InternalTable, DataFile, Matrix, UserTable, DataClipboard, Unknown
    }

    public DataDefinition() {
        initDefinition();
        dataType = DataType.DataFile;
    }

    public DataDefinition(DataType dataType, String dataName) {
        initDefinition();
        this.dataType = dataType;
        this.dataName = dataName;
    }

    private void initDefinition() {
        dfid = -1;
        file = null;
        dataName = null;
        hasHeader = false;
        charset = Charset.defaultCharset().name();
        delimiter = ",";
        colsNumber = rowsNumber = 0;
        scale = 2;
        maxRandom = 1000;
        modifyTime = new Date();
        comments = null;
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
            case "file":
                return data.getFile() == null ? null : data.getFile().getAbsolutePath();
            case "charset":
                return data.getCharset();
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
                case "file":
                    data.setFile(value == null ? null : new File((String) value));
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

    public static StringTable saveDefinition(TableDataDefinition tableDataDefinition, TableDataColumn tableDataColumn,
            String dataName, DataDefinition.DataType dataType,
            Charset charset, String delimiterName, boolean withName, List<ColumnDefinition> columns) {
        if (dataName == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveDefinition(tableDataDefinition, tableDataColumn,
                    conn, dataName, dataType, charset, delimiterName, withName, columns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static StringTable saveDefinition(TableDataDefinition tableDataDefinition, TableDataColumn tableDataColumn,
            Connection conn, String dataName, DataDefinition.DataType dataType,
            Charset charset, String delimiterName, boolean withName, List<ColumnDefinition> columns) {
        if (conn == null || dataName == null) {
            return null;
        }
        try {
            DataDefinition def = tableDataDefinition.queryName(conn, dataType, dataName);
            if (def == null) {
                def = DataDefinition.create()
                        .setDataName(dataName).setDataType(dataType)
                        .setCharset(charset.name()).setHasHeader(withName).setDelimiter(delimiterName);
                tableDataDefinition.insertData(conn, def);
            } else {
                def.setCharset(charset.name()).setHasHeader(withName).setDelimiter(delimiterName);
                tableDataDefinition.updateData(conn, def);
                tableDataColumn.clear(conn, def.getDfid());
            }
            StringTable validateTable = null;
            if (columns != null && !columns.isEmpty()) {
                validateTable = ColumnDefinition.validate(columns);
                if (validateTable != null && validateTable.isEmpty()) {
                    tableDataColumn.save(conn, def.getDfid(), columns);
                    conn.commit();
                }
            }
            return validateTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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

    public String getComments() {
        return comments;
    }

    public DataDefinition setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public long getColsNumber() {
        return colsNumber;
    }

    public DataDefinition setColsNumber(long colsNumber) {
        this.colsNumber = colsNumber;
        return this;
    }

    public long getRowsNumber() {
        return rowsNumber;
    }

    public DataDefinition setRowsNumber(long rowsNumber) {
        this.rowsNumber = rowsNumber;
        return this;
    }

    public short getScale() {
        return scale;
    }

    public DataDefinition setScale(short scale) {
        this.scale = scale;
        return this;
    }

    public int getMaxRandom() {
        return maxRandom;
    }

    public DataDefinition setMaxRandom(int maxRandom) {
        this.maxRandom = maxRandom;
        return this;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public DataDefinition setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
        return this;
    }

    public File getFile() {
        return file;
    }

    public DataDefinition setFile(File file) {
        this.file = file;
        return this;
    }

}
