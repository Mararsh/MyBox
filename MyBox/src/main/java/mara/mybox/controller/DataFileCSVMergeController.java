package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-2-13
 * @License Apache License Version 2.0
 */
public class DataFileCSVMergeController extends FilesMergeController {

    protected CSVFormat sourceFormat, targetFormat;
    protected Charset sourceCharset, targetCharset;
    protected CSVPrinter csvPrinter;
    protected List<String> headers;

    @FXML
    protected ControlCsvOptions csvSourceController, csvTargetController;

    public DataFileCSVMergeController() {
        baseTitle = AppVariables.message("CsvMerge");

        SourceFileType = VisitHistory.FileType.CSV;
        SourcePathType = VisitHistory.FileType.CSV;
        AddFileType = VisitHistory.FileType.CSV;
        AddPathType = VisitHistory.FileType.CSV;
        TargetPathType = VisitHistory.FileType.CSV;
        TargetFileType = VisitHistory.FileType.CSV;
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.CSV);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.CSV);
        sourceExtensionFilter = CommonFxValues.CsvExtensionFilter;
        targetExtensionFilter = CommonFxValues.CsvExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableData)
                    .or(Bindings.isEmpty(targetFileInput.textProperty()))
                    .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                    .or(csvSourceController.delimiterInput.styleProperty().isEqualTo(badStyle))
                    .or(csvTargetController.delimiterInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            csvSourceController.setControls(baseName + "Source");
            csvTargetController.setControls(baseName + "Target");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected boolean openWriter() {
        try {
            if (csvSourceController.delimiterInput.getStyle().equals(badStyle)
                    || (!csvSourceController.autoDetermine && csvSourceController.charset == null)) {
                return false;
            }
            sourceFormat = CSVFormat.DEFAULT.withDelimiter(csvSourceController.delimiter)
                    .withIgnoreEmptyLines().withTrim().withNullString("");
            if (csvSourceController.withNamesCheck.isSelected()) {
                sourceFormat = sourceFormat.withFirstRecordAsHeader();
            }
            sourceCharset = csvSourceController.charset;

            targetFormat = CSVFormat.DEFAULT
                    .withDelimiter(csvTargetController.delimiter)
                    .withIgnoreEmptyLines().withTrim().withNullString("");
            if (csvTargetController.withNamesCheck.isSelected()) {
                targetFormat = targetFormat.withFirstRecordAsHeader();
            }
            targetCharset = csvTargetController.charset;
            csvPrinter = new CSVPrinter(new FileWriter(targetFile, targetCharset), targetFormat);

            headers = null;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile) {
        if (csvSourceController.autoDetermine) {
            sourceCharset = FileTools.charset(srcFile);
        }
        String result;
        try ( CSVParser parser = CSVParser.parse(srcFile, sourceCharset, sourceFormat)) {
            if (csvTargetController.withNamesCheck.isSelected() && headers == null
                    && csvSourceController.withNamesCheck.isSelected()) {
                headers = new ArrayList<>();
                headers.addAll(parser.getHeaderNames());
                csvPrinter.printRecord(headers);
            }
            List<String> rowData = new ArrayList<>();
            for (CSVRecord record : parser) {
                for (int i = 0; i < record.size(); i++) {
                    rowData.add(record.get(i));
                }
                if (csvTargetController.withNamesCheck.isSelected() && headers == null) {
                    headers = new ArrayList<>();
                    for (int i = 0; i < rowData.size(); i++) {
                        headers.add(message("Field") + i);
                    }
                    csvPrinter.printRecord(headers);
                }
                csvPrinter.printRecord(rowData);
                rowData.clear();
            }
            result = message("Handled") + ": " + srcFile;
        } catch (Exception e) {
            result = srcFile + " " + e.toString();
        }
        return result;
    }

    @Override
    protected boolean closeWriter() {
        try {
            csvPrinter.flush();
            csvPrinter.close();
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                TableDataDefinition tableDataDefinition = new TableDataDefinition();
                tableDataDefinition.clear(conn, DataDefinition.DataType.DataFile, targetFile.getAbsolutePath());
                conn.commit();
                DataDefinition dataDefinition = DataDefinition.create()
                        .setDataName(targetFile.getAbsolutePath())
                        .setDataType(DataDefinition.DataType.DataFile)
                        .setCharset(targetCharset.name())
                        .setHasHeader(csvTargetController.withNamesCheck.isSelected())
                        .setDelimiter(csvTargetController.delimiter + "");
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } catch (Exception e) {
                updateLogs(e.toString(), true, true);
                return false;
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

}
