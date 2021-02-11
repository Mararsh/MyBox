package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
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
 * @CreateDate 2021-1-30
 * @License Apache License Version 2.0
 */
public class DataFileCSVSplitController extends BaseBatchFileController {

    protected CSVFormat sourceFormat, targetFormat;
    protected Charset sourceCharset, targetCharset;
    protected CSVPrinter csvPrinter;
    protected String filePrefix;
    protected List<String> headers;
    protected int maxLines, fileIndex, rowIndex;

    @FXML
    protected ControlCsvOptions sourceCSVController, targetCSVController;
    @FXML
    protected ComboBox<String> maxLinesSelector;

    public DataFileCSVSplitController() {
        baseTitle = AppVariables.message("CsvSplit");

        SourceFileType = VisitHistory.FileType.CSV;
        SourcePathType = VisitHistory.FileType.CSV;
        AddFileType = VisitHistory.FileType.CSV;
        AddPathType = VisitHistory.FileType.CSV;
        TargetPathType = VisitHistory.FileType.All;
        TargetFileType = VisitHistory.FileType.All;
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.CSV);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.All);
        sourceExtensionFilter = CommonFxValues.CsvExtensionFilter;
        targetExtensionFilter = CommonFxValues.AllExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
                    .or(Bindings.isEmpty(targetPathInput.textProperty()))
                    .or(sourceCSVController.delimiterInput.styleProperty().isEqualTo(badStyle))
                    .or(maxLinesSelector.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            sourceCSVController.setControls(baseName + "Source");
            targetCSVController.setControls(baseName + "Target");

            maxLines = AppVariables.getUserConfigInt(baseName + "Lines", 1000);
            maxLines = maxLines < 1 ? 1000 : maxLines;
            maxLinesSelector.getItems().addAll(Arrays.asList(
                    "1000", "500", "200", "300", "800", "2000", "3000", "5000", "8000"
            ));
            maxLinesSelector.setValue(maxLines + "");
            maxLinesSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                maxLines = v;
                                AppVariables.setUserConfigInt(baseName + "Lines", v);
                                FxmlControl.setEditorNormal(maxLinesSelector);
                            } else {
                                FxmlControl.setEditorBadStyle(maxLinesSelector);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(maxLinesSelector);
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public boolean makeMoreParameters() {
        if (sourceCSVController.delimiterInput.getStyle().equals(badStyle)
                || (!sourceCSVController.autoDetermine && sourceCSVController.charset == null)) {
            return false;
        }
        sourceFormat = CSVFormat.DEFAULT.withDelimiter(sourceCSVController.delimiter)
                .withIgnoreEmptyLines().withTrim().withNullString("");
        if (sourceCSVController.withNamesCheck.isSelected()) {
            sourceFormat = sourceFormat.withFirstRecordAsHeader();
        }
        sourceCharset = sourceCSVController.charset;

        targetFormat = CSVFormat.DEFAULT
                .withDelimiter(targetCSVController.delimiter)
                .withIgnoreEmptyLines().withTrim().withNullString("");
        if (targetCSVController.withNamesCheck.isSelected()) {
            targetFormat = targetFormat.withFirstRecordAsHeader();
        }
        targetCharset = targetCSVController.charset;
        csvPrinter = null;
        filePrefix = null;
        fileIndex = rowIndex = 0;
        headers = null;
        targetExistType = TargetExistType.Replace;
        maxLines = maxLines < 1 ? 1000 : maxLines;
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        countHandling(srcFile);
        if (sourceCSVController.autoDetermine) {
            sourceCharset = FileTools.charset(srcFile);
        }
        if (targetCSVController.autoDetermine) {
            targetCharset = sourceCharset;
        }
        filePrefix = FileTools.getFilePrefix(srcFile.getName());
        String result = null;
        try ( CSVParser parser = CSVParser.parse(srcFile, sourceCharset, sourceFormat)) {
            if (sourceCSVController.withNamesCheck.isSelected()) {
                headers = parser.getHeaderNames();
            }
            List<String> rowData = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (sourceCSVController.withNamesCheck.isSelected()) {
                    for (String name : headers) {
                        rowData.add(record.get(name));
                    }
                } else {
                    for (int i = 0; i < record.size(); i++) {
                        rowData.add(record.get(i));
                    }
                    if (headers == null) {
                        headers = new ArrayList<>();
                        for (int i = 0; i < rowData.size(); i++) {
                            headers.add(message("Field") + i);
                        }
                    }
                }
                result = checkPrinter();
                if (result != null) {
                    break;
                }
                csvPrinter.printRecord(rowData);
                rowIndex++;
                rowData.clear();
            }
            if (csvPrinter != null) {
                csvPrinter.flush();
                csvPrinter.close();
                if (rowIndex < maxLines) {
                    int startRow = (fileIndex - 1) * maxLines + 1;
                    File aFile = makeTargetFile(filePrefix + "_" + startRow + "-" + (startRow + rowIndex - 1), ".csv", targetPath);
                    FileTools.rename(targetFile, aFile);
                }
                targetFileGenerated(targetFile);
            }
            if (result == null) {
                result = message("Handled");
            }
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

    protected String checkPrinter() {
        if (csvPrinter != null && rowIndex <= maxLines) {
            return null;
        }
        try {
            if (csvPrinter != null) {
                csvPrinter.flush();
                csvPrinter.close();
                targetFileGenerated(targetFile);
                rowIndex = 1;
            }
            int startRow = (fileIndex++) * maxLines + 1;
            targetFile = makeTargetFile(filePrefix + "_" + startRow + "-" + (startRow + maxLines - 1), ".csv", targetPath);
            updateLogs(message("Writing") + " " + targetFile.getAbsolutePath());
            csvPrinter = new CSVPrinter(new FileWriter(targetFile, targetCharset), targetFormat);
            if (headers != null && targetCSVController.withNamesCheck.isSelected()) {
                csvPrinter.printRecord(headers);
            }
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

}
