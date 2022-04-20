package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mara.mybox.data2d.scan.Data2DReader;
import mara.mybox.data2d.scan.Data2DReader.Operation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Edit extends Data2D_Data {

    public abstract Data2DDefinition queryDefinition(Connection conn);

    public abstract void applyOptions();

    public abstract List<String> readColumnNames();

    public abstract boolean savePageData(Data2D targetData);

    public abstract boolean setValue(List<Integer> cols, String value);

    public abstract long clearData();

    /*
        read
     */
    public boolean checkForLoad() {
        return true;
    }

    public long readDataDefinition(Connection conn) {
        if (isTmpData()) {
            checkForLoad();
            return -1;
        }
        try {
            Data2DDefinition definition = queryDefinition(conn);
            if (definition != null) {
                cloneAll(definition);
            }
            applyOptions();
            checkForLoad();
            if (definition == null) {
                definition = tableData2DDefinition.insertData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
            } else {
                tableData2DDefinition.updateData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
                savedColumns = tableData2DColumn.read(conn, d2did);
            }
            options = null;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return -1;
        }
        return d2did;
    }

    public boolean readColumns(Connection conn) {
        try {
            columns = null;
            List<String> colNames = readColumnNames();
            if (colNames == null || colNames.isEmpty()) {
                hasHeader = false;
                columns = savedColumns;
            } else {
                List<String> validNames = new ArrayList<>();
                columns = new ArrayList<>();
                for (int i = 0; i < colNames.size(); i++) {
                    String name = colNames.get(i);
                    Data2DColumn column;
                    if (savedColumns != null && i < savedColumns.size()) {
                        column = savedColumns.get(i);
                        if (!hasHeader) {
                            name = column.getColumnName();
                        }
                    } else {
                        column = new Data2DColumn(name, defaultColumnType());
                    }
                    String vname = (name == null || name.isBlank()) ? message("Column") + (i + 1) : name;
                    while (validNames.contains(vname)) {
                        vname += "m";
                    }
                    validNames.add(vname);
                    column.setColumnName(vname);
                    columns.add(column);
                }
            }
            if (columns != null && !columns.isEmpty()) {
                Random random = new Random();
                List<String> names = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    Data2DColumn column = columns.get(i);
                    column.setD2id(d2did);
                    column.setIndex(i);
                    if (column.getColor() == null) {
                        column.setColor(FxColorTools.randomColor(random));
                    }
                    if (!isTable()) {
                        column.setAuto(false);
                        column.setIsPrimaryKey(false);
                    }
                    if (isMatrix()) {
                        column.setType(ColumnDefinition.ColumnType.Double);
                    }
                    names.add(column.getColumnName());
                }
                colsNumber = columns.size();
                if (d2did >= 0) {
                    tableData2DColumn.save(conn, d2did, columns);
                    tableData2DDefinition.updateData(conn, this);
                    tableData2DStyle.checkColumns(conn, d2did, names);
                }
            } else {
                colsNumber = 0;
                if (d2did >= 0) {
                    tableData2DColumn.clear(conn, d2did);
                    tableData2DDefinition.updateData(conn, this);
                    tableData2DStyle.clear(conn, d2did);
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return false;
        }
    }

    public long readTotal() {
        dataSize = 0;
        Data2DReader reader = Data2DReader.create(this)
                .setReaderTask(backgroundTask).start(Operation.ReadTotal);
        if (reader != null) {
            dataSize = reader.getRowIndex();
        }
        rowsNumber = dataSize;
        try ( Connection conn = DerbyBase.getConnection()) {
            tableData2DDefinition.updateData(conn, this);
            tableData2DStyle.checkRows(conn, d2did, dataSize);
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        return dataSize;
    }

    public List<List<String>> readPageData(Connection conn) {
        if (!isColumnsValid()) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        Data2DReader reader = Data2DReader.create(this)
                .setConn(conn).setReaderTask(task)
                .start(Operation.ReadPage);
        if (reader == null) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        List<List<String>> rows = reader.getRows();
        if (rows != null) {
            endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        }
        readPageStyles(conn);
        return rows;
    }

    public void readPageStyles(Connection conn) {
        styles.clear();
        if (d2did < 0 || startRowOfCurrentPage >= endRowOfCurrentPage) {
            return;
        }
        try ( PreparedStatement statement = conn.prepareStatement(TableData2DStyle.QueryPageStyles)) {
            statement.setLong(1, d2did);
            statement.setLong(2, startRowOfCurrentPage);
            statement.setLong(3, endRowOfCurrentPage);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                Data2DStyle d = tableData2DStyle.readData(results);
                if (d == null) {
                    continue;
                }
                setStyle(d.getRow() - startRowOfCurrentPage, d.getColName(), d.getStyle());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void countSize() {
        try {
            rowsNumber = dataSize + (tableRowsNumber() - (endRowOfCurrentPage - startRowOfCurrentPage));
            colsNumber = tableColsNumber();
            if (colsNumber <= 0) {
                hasHeader = false;
            }
        } catch (Exception e) {
        }
    }

    /*
        write
     */
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            if (file != null && !isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
        }
        return true;
    }

    public boolean saveAttributes() {
        countSize();
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveColumns(conn, (Data2D) this, columns)
                    && savePageStyles(conn);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean savePageStyles(Connection conn) {
        if (conn == null || d2did < 0) {
            return false;
        }
        try ( Statement statement = conn.createStatement()) {
            if (endRowOfCurrentPage > startRowOfCurrentPage) {
                String sql = "DELETE FROM Data2D_Style WHERE d2id=" + d2did
                        + " AND row>=" + startRowOfCurrentPage + " AND row<" + endRowOfCurrentPage;
                statement.executeUpdate(sql);
                conn.commit();
            }
            if (isMutiplePages()) {
                long offset = tableRowsNumber() - (endRowOfCurrentPage - startRowOfCurrentPage);
                if (offset != 0) {
                    String sql = "UPDATE Data2D_Style SET row=row+" + offset
                            + " WHERE d2id=" + d2did + " AND row >= " + endRowOfCurrentPage;
                    statement.executeUpdate(sql);
                    conn.commit();
                }
            }
            if (styles.isEmpty()) {
                return true;
            }
            conn.setAutoCommit(false);
            long count = 0;
            for (String key : styles.keySet()) {
                String style = styles.get(key);
                int pos = key.indexOf(",");
                int row = Integer.valueOf(key.substring(0, pos));
                String colName = key.substring(pos + 1);
                Data2DStyle d2Style = new Data2DStyle(d2did, row + startRowOfCurrentPage, colName, style);
                tableData2DStyle.write(conn, d2Style);
                if (++count >= DerbyBase.BatchSize) {
                    conn.commit();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean saveStyles(List<String> cols, String style) {
        if (cols == null || cols.isEmpty() || tableChanged || d2did < 0) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            if (dataSize <= 0) {
                String in = null;
                for (String col : cols) {
                    if (in == null) {
                        in = col;
                    } else {
                        in += "," + col;
                    }
                }
                String sql = "DELETE FROM Data2D_Style WHERE d2id=" + d2did;
                statement.executeUpdate(sql);
                conn.commit();
            } else if (style == null || style.isBlank()) {
                String in = null;
                for (String col : cols) {
                    if (in == null) {
                        in = "'" + col + "'";
                    } else {
                        in += ", '" + col + "'";
                    }
                }
                String sql = "DELETE FROM Data2D_Style WHERE d2id=" + d2did
                        + " AND colName IN (" + in + ")";
                statement.executeUpdate(sql);
                conn.commit();
            } else {
                conn.setAutoCommit(false);
                long count = 0;
                for (int row = 0; row < dataSize; row++) {
                    for (String colName : cols) {
                        Data2DStyle d2Style = new Data2DStyle(d2did, row, colName, style);
                        tableData2DStyle.write(conn, d2Style);
                    }
                    if (++count >= DerbyBase.BatchSize) {
                        conn.commit();
                    }
                }
                conn.commit();
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveAttributes(Data2D source, Data2D target) {
        try ( Connection conn = DerbyBase.getConnection()) {
            source.countSize();
            target.cloneAttributes(source);
            if (!saveColumns(conn, target, source.getColumns())) {
                return false;
            }
            long targetid = target.getD2did();
            target.getTableData2DStyle().clear(conn, targetid);
            target.getTableData2DStyle().copyStyles(conn, source.getD2did(), target.getD2did());
            return target.savePageStyles(conn);
        } catch (Exception e) {
            if (source.getTask() != null) {
                source.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveColumns(Data2D d, List<Data2DColumn> cols) {
        if (d == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveColumns(conn, d, cols);
        } catch (Exception e) {
            if (d.getTask() != null) {
                d.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveColumns(Connection conn, Data2D d, List<Data2DColumn> inColumns) {
        if (d == null) {
            return false;
        }
        try {
            if (!d.checkForSave() || !d.checkForLoad()) {
                return false;
            }
            Data2DDefinition def;
            long did = d.getD2did();
            d.setModifyTime(new Date());
            d.setColsNumber(inColumns == null ? 0 : inColumns.size());
            if (did >= 0) {
                def = d.getTableData2DDefinition().updateData(conn, d);
            } else {
                def = d.queryDefinition(conn);
                if (def == null) {
                    def = d.getTableData2DDefinition().insertData(conn, d);
                } else {
                    d.setD2did(def.getD2did());
                    def = d.getTableData2DDefinition().updateData(conn, d);
                }
            }
            conn.commit();
            did = def.getD2did();
            if (did < 0) {
                return false;
            }
            d.cloneAll(def);
            if (inColumns != null && !inColumns.isEmpty()) {
                try {
                    List<Data2DColumn> savedColumns = new ArrayList<>();
                    for (int i = 0; i < inColumns.size(); i++) {
                        Data2DColumn column = inColumns.get(i).cloneAll();
                        if (column.getD2id() != did) {
                            column.setD2cid(-1);
                        }
                        column.setD2id(did);
                        column.setIndex(i);
                        if (!d.isTable()) {
                            column.setIsPrimaryKey(false);
                            column.setAuto(false);
                        }
                        if (d.isMatrix()) {
                            column.setType(ColumnDefinition.ColumnType.Double);
                        }
                        savedColumns.add(column);
                    }
                    d.getTableData2DColumn().save(conn, did, savedColumns);
                    d.setColumns(savedColumns);
                } catch (Exception e) {
                    if (d.getTask() != null) {
                        d.getTask().setError(e.toString());
                    }
                    MyBoxLog.error(e);
                }
            } else {
                d.getTableData2DColumn().clear(d);
                d.setColumns(null);
            }
            return true;
        } catch (Exception e) {
            if (d.getTask() != null) {
                d.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

}
