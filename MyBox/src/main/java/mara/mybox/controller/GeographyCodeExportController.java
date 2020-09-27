package mara.mybox.controller;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.tools.GeographyCodeTools;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.value.AppVariables;
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
    protected void writeInternalCSVHeader(CSVPrinter printer) {
        GeographyCodeTools.writeInternalCSVHeader(printer);
    }

    @Override
    protected void writeInternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        GeographyCode code = TableGeographyCode.readResults(results);
        GeographyCodeTools.writeInternalCSV(printer, code);
    }

    @Override
    protected void writeExternalCSVHeader(CSVPrinter printer) {
        GeographyCodeTools.writeExternalCSVHeader(printer);
    }

    @Override
    protected void writeExternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        GeographyCodeTools.writeExternalCSV(printer, code);
    }

    @Override
    protected void writeXML(Connection conn, FileWriter writer, ResultSet results, String indent) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        GeographyCodeTools.writeXml(writer, indent, code);
    }

    @Override
    protected String writeJSON(Connection conn, FileWriter writer, ResultSet results, String indent) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        return GeographyCodeTools.writeJson(writer, indent, code).toString();
    }

    @Override
    protected List<String> columnLabels() {
        return new TableGeographyCode().importAllFields();
    }

    @Override
    protected void writeExcel(Connection conn, XSSFSheet sheet, ResultSet results, int count) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        GeographyCodeTools.writeExcel(sheet, count, code);
    }

    @Override
    protected List<String> htmlRow(Connection conn, ResultSet results) {
        GeographyCode code = TableGeographyCode.readResults(results);
        TableGeographyCode.decodeAncestors(conn, code);
        return GeographyCodeTools.externalValues(code);
    }

}
