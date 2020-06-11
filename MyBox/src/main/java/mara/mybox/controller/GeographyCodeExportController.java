package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * @Author Mara
 * @CreateDate 2020-03-30
 * @License Apache License Version 2.0
 */
public class GeographyCodeExportController extends DataExportController {

    public GeographyCodeExportController() {
        baseTitle = AppVariables.message("ExportGeographyCodes");
        baseName = "GeographyCode";
    }

    @Override
    protected void setControls() {
        try {
            super.setControls();
            tableArea.setText(new TableGeographyCode().getCreate_Table_Statement());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void writeInternalCSVHeader(CSVPrinter printer) {
        GeographyCode.writeInternalCSVHeader(printer);
    }

    @Override
    protected void writeInternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        GeographyCode code = TableGeographyCode.readResults(results);
        GeographyCode.writeInternalCSV(printer, code);
    }

    @Override
    protected void writeExternalCSVHeader(CSVPrinter printer) {
        GeographyCode.writeExternalCSVHeader(printer);
    }

    @Override
    protected void writeExternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        GeographyCode.writeExternalCSV(printer, code);
    }

    @Override
    protected void writeXML(Connection conn, FileWriter writer, ResultSet results, String indent) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        GeographyCode.writeXml(writer, indent, code);
    }

    @Override
    protected String writeJSON(Connection conn, FileWriter writer, ResultSet results, String indent) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        return GeographyCode.writeJson(writer, indent, code).toString();
    }

    @Override
    protected List<String> columnNames() {
        return GeographyCode.externalNames();
    }

    @Override
    protected void writeExcel(Connection conn, XSSFSheet sheet, ResultSet results, int count) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        GeographyCode.writeExcel(sheet, count, code);
    }

    @Override
    protected List<String> htmlRow(Connection conn, ResultSet results) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        return GeographyCode.values(code);
    }

    public void startAction2() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            cancelled = false;
            synchronized (this) {
                taskCount = 0;
            }
            tabPane.getSelectionModel().select(logsTab);
            startTime = new Date().getTime();
            initLogs();
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        String path = targetPath.getAbsolutePath() + File.separator;
                        String filePrefix = path + File.separator;
                        String sql = "SELECT * FROM Geography_Code WHERE "
                                + " country=100 AND level=8 AND province=1014 ORDER BY gcid ";
                        updateLogs(sql);
                        writeJSON(new File(filePrefix + "山东_villages.json"), sql);
                        writeXML(new File(filePrefix + "山东_villages.xml"), sql);
                        writeExternalCSV(new File(filePrefix + "山东_external.csv"), sql);

                        sql = "SELECT * FROM Geography_Code WHERE "
                                + " country=100 AND level=8 AND province=1022 ORDER BY gcid ";
                        updateLogs(sql);
                        writeExternalCSV(new File(filePrefix + "四川_external.csv"), sql);

                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        updateLogs(error);
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    browseURI(targetPath.toURI());
                }
            };
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
