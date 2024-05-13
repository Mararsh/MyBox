package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.data2d.writer.DataTableWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.tools.FileTmpTools;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class DataTableSaveAttributes extends DataTableModify {

    protected Data2D attributes;
    protected DataFileCSVWriter csvWriter;

    public DataTableSaveAttributes(DataTable data, Data2D attrs) {
        if (!setSourceData(data)) {
            return;
        }
        sourceTable = data;
        attributes = attrs;
    }

    @Override
    public boolean checkParameters() {
        try {
            csvWriter = new DataFileCSVWriter();
            csvWriter.setPrintFile(FileTmpTools.getTempFile(".csv"));
            csvWriter.setColumns(attributes.getColumns())
                    .setHeaderNames(attributes.columnNames())
                    .setWriteHeader(true)
                    .setOperate(this);
            return super.checkParameters();
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }

    }

    @Override
    public boolean go() {
        handledCount = 0;
        tableData2D = sourceTable.getTableData2D();
        tableData2D.setTableName(sourceTable.getSheet());
        String sql = "SELECT * FROM " + sourceTable.getSheet();
        showInfo(sql);
        columns = sourceTable.getColumns();
        columnsNumber = columns.size();
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement statement = dconn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                PreparedStatement dUpdate = conn.prepareStatement(tableData2D.updateStatement())) {
            conn = dconn;
            conn.setAutoCommit(false);
            update = dUpdate;
            csvWriter.openWriter();
            while (results.next() && !stopped && !reachMax) {
                sourceTableRow = tableData2D.readData(results);
                sourceRow = sourceTableRow.toStrings(columns);
                sourceRowIndex++;
                applyAttributes(sourceRow, sourceRowIndex);
            }
            if (!stopped) {
                failed = updateTable();
            }
            conn.close();
            conn = null;
            return failed;
        } catch (Exception e) {
            failStop(e.toString());
            return false;
        }
    }

    @Override
    public void writeRow() {
        try {
            if (stopped || targetRow == null || targetRow.isEmpty()) {
                return;
            }
            csvWriter.writeRow(targetRow);
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public boolean updateTable() {
        try {
            if (stopped || sourceTable == null || conn == null) {
                return false;
            }
            csvWriter.closeWriter();
            Data2D tmpData = csvWriter.getTargetData();
            if (tmpData == null) {
                return false;
            }
            DataTableWriter tableWriter = new DataTableWriter();
            tableWriter.setTargetTableName(sourceTable.getSheet())
                    .setKeys(null)
                    .setIdName("id")
                    .setTargetTableDesciption(sourceTable.getComments())
                    .setDropExisted(true)
                    .setRecordTargetFile(false)
                    .setRecordTargetData(true);
            ssdsdsd
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

}
