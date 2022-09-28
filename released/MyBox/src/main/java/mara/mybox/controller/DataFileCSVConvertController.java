package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-12-14
 * @License Apache License Version 2.0
 */
public class DataFileCSVConvertController extends BaseDataConvertController {

    @FXML
    protected ControlTextOptions csvReadController;

    public DataFileCSVConvertController() {
        baseTitle = message("CsvConvert");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV, VisitHistory.FileType.All);
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            csvReadController.setControls(baseName + "Read", true, false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (csvReadController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())
                || (!csvReadController.autoDetermine && csvReadController.charset == null)) {
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        if (csvReadController.withNamesCheck.isSelected()) {
            return withHeader(srcFile, targetPath);
        } else {
            return withoutHeader(srcFile, targetPath);
        }
    }

    public String withHeader(File srcFile, File targetPath) {
        String result;
        Charset fileCharset;
        if (csvReadController.autoDetermine) {
            fileCharset = TextFileTools.charset(srcFile);
        } else {
            fileCharset = csvReadController.charset;
        }
        File validFile = FileTools.removeBOM(srcFile);
        try ( CSVParser parser = CSVParser.parse(validFile, fileCharset,
                CsvTools.csvFormat(csvReadController.getDelimiterName(), true))) {
            List<String> names = new ArrayList<>();
            names.addAll(parser.getHeaderNames());
            convertController.setParameters(targetPath, names, filePrefix(srcFile), skip);
            for (CSVRecord record : parser) {
                if (task == null || task.isCancelled()) {
                    return message("Cancelled");
                }
                List<String> rowData = new ArrayList<>();
                for (String v : record) {
                    rowData.add(v);
                }
                convertController.writeRow(rowData);
            }
            convertController.closeWriters();
            result = message("Handled");
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

    public String withoutHeader(File srcFile, File targetPath) {
        String result;
        Charset fileCharset;
        if (csvReadController.autoDetermine) {
            fileCharset = TextFileTools.charset(srcFile);
        } else {
            fileCharset = csvReadController.charset;
        }
        File validFile = FileTools.removeBOM(srcFile);
        try ( CSVParser parser = CSVParser.parse(validFile, fileCharset,
                CsvTools.csvFormat(csvReadController.getDelimiterName(), false))) {
            List<String> names = null;
            for (CSVRecord record : parser) {
                if (task == null || task.isCancelled()) {
                    return message("Cancelled");
                }
                if (names == null) {
                    names = new ArrayList<>();
                    for (int i = 1; i <= record.size(); i++) {
                        names.add(message("Column") + i);
                    }
                    convertController.setParameters(targetPath, names, filePrefix(srcFile), skip);
                }
                List<String> rowData = new ArrayList<>();
                for (String v : record) {
                    rowData.add(v);
                }
                convertController.writeRow(rowData);
            }
            convertController.closeWriters();
            result = message("Handled");
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

}
