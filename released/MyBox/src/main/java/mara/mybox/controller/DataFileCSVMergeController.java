package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
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
    protected boolean sourceWithName, targetWithName;

    @FXML
    protected ControlTextOptions csvSourceController, csvTargetController;

    public DataFileCSVMergeController() {
        baseTitle = Languages.message("CsvMerge");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableData)
                    .or(targetFileController.valid.not())
                    .or(csvSourceController.delimiterController.delimiterInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(csvTargetController.delimiterController.delimiterInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            csvSourceController.setControls(baseName + "Source", true, false);
            csvTargetController.setControls(baseName + "Target", false, false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected boolean openWriter() {
        try {
            if (csvSourceController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())
                    || (!csvSourceController.autoDetermine && csvSourceController.charset == null)) {
                return false;
            }
            sourceCharset = csvSourceController.charset;
            sourceWithName = csvSourceController.withNamesCheck.isSelected();
            sourceFormat = CsvTools.csvFormat(csvSourceController.getDelimiterName(), sourceWithName);

            targetCharset = csvTargetController.charset;
            targetWithName = csvTargetController.withNamesCheck.isSelected();
            targetFormat = CsvTools.csvFormat(csvTargetController.getDelimiterName(), targetWithName);

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
            sourceCharset = TextFileTools.charset(srcFile);
        }
        String result;
        File validFile = FileTools.removeBOM(srcFile);
        try ( CSVParser parser = CSVParser.parse(validFile, sourceCharset, sourceFormat)) {
            if (headers == null && targetWithName && sourceWithName) {
                headers = new ArrayList<>();
                headers.addAll(parser.getHeaderNames());
                csvPrinter.printRecord(headers);
            }
            List<String> rowData = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (task == null || task.isCancelled()) {
                    return message("Cancelled");
                }
                for (int i = 0; i < record.size(); i++) {
                    rowData.add(record.get(i));
                }
                if (headers == null && targetWithName) {
                    headers = new ArrayList<>();
                    for (int i = 0; i < rowData.size(); i++) {
                        headers.add(Languages.message("Column") + i);
                    }
                    csvPrinter.printRecord(headers);
                }
                csvPrinter.printRecord(rowData);
                rowData.clear();
            }
            result = Languages.message("Handled") + ": " + srcFile;
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
            try ( Connection conn = DerbyBase.getConnection()) {
                TableData2DDefinition tableData2DDefinition = new TableData2DDefinition();
                Data2DDefinition def = tableData2DDefinition.queryFile(conn, Data2DDefinition.Type.CSV, targetFile);
                if (def == null) {
                    def = Data2DDefinition.create();
                }
                def.setType(Data2DDefinition.Type.CSV)
                        .setFile(targetFile)
                        .setDataName(targetFile.getName())
                        .setCharset(targetCharset)
                        .setHasHeader(csvTargetController.withNamesCheck.isSelected())
                        .setDelimiter(csvTargetController.getDelimiterName());
                if (def.getD2did() < 0) {
                    tableData2DDefinition.insertData(conn, def);
                } else {
                    tableData2DDefinition.updateData(conn, def);
                }
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
