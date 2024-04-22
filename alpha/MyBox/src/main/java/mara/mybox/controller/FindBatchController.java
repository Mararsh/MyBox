package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-11-4
 * @License Apache License Version 2.0
 */
public abstract class FindBatchController extends FindReplaceBatchController {

    protected File mergedFile;
    protected CSVPrinter csvPrinter;
    protected long mergedCount;
    protected DataFileCSV mergedData;

    @FXML
    protected CheckBox mergeCheck;

    public FindBatchController() {
        baseTitle = message("TextFindBatch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            mergeCheck.setSelected(UserConfig.getBoolean(baseName + "Merge", true));
            mergeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Merge", nv);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            if (!super.makeMoreParameters()) {
                return false;
            }
            if (csvPrinter != null) {
                csvPrinter.close();
                csvPrinter = null;
            }
            mergedCount = 0;
            mergedData = null;
            if (mergeCheck.isSelected()) {
                mergedFile = FileTmpTools.getTempFile();
                csvPrinter = CsvTools.csvPrinter(mergedFile);
                List<String> names = new ArrayList<>();
                names.addAll(Arrays.asList(message("File"), message("Start"), message("End"), message("String")));
                csvPrinter.printRecord(names);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            FileEditInformation info = info(currentTask, srcFile);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Cancelled");
            }
            if (info == null) {
                return message("Failed");
            }
            if (!findReplace.handleFile(currentTask)) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return message("Canceled");
                }
                return message("Failed");
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Cancelled");
            }
            DataFileCSV matchesData = findReplace.getMatchesData();
            int count = findReplace.getCount();
            if (count == 0 || matchesData == null) {
                return message("NotFound");
            }
            File matchesFile = matchesData.getFile();
            if (matchesFile == null || !matchesFile.exists()) {
                return message("NotFound");
            }
            if (csvPrinter != null) {
                try (CSVParser parser = CSVParser.parse(matchesFile, Charset.forName("utf-8"), CsvTools.csvFormat())) {
                    Iterator<CSVRecord> iterator = parser.iterator();
                    while (iterator.hasNext()) {
                        if (task == null || task.isCancelled()) {
                            parser.close();
                            return message("Cancelled");
                        }
                        CSVRecord csvRecord = iterator.next();
                        if (csvRecord == null) {
                            continue;
                        }
                        List<String> row = new ArrayList<>();
                        row.add(srcFile.getAbsolutePath());
                        for (String v : csvRecord) {
                            row.add(v);
                        }
                        csvPrinter.printRecord(row);
                        mergedCount++;
                    }
                    parser.close();
                } catch (Exception e) {
                    showLogs(e.toString());
                    return e.toString();
                }
                matchesFile.delete();
            } else {
                targetFileGenerated(matchesFile);
            }
            return message("Found") + ": " + count;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    @Override
    public void afterTask(boolean ok) {
        try {
            if (csvPrinter != null) {
                csvPrinter.close();
                csvPrinter = null;
                if (mergedFile != null && mergedFile.exists() && mergedCount > 0) {
                    mergedData = findReplace.initMatchesData(null);
                    File matchesFile = mergedData.getFile();
                    if (FileTools.override(mergedFile, matchesFile)) {
                        mergedData.setRowsNumber(mergedCount);
                        targetFileGenerated(matchesFile);
                    }
                }
            }
            super.afterTask(ok);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void openTarget() {
        try {
            if (mergedData != null) {
                Data2DManufactureController.openDef(mergedData);
            } else {
                super.openTarget();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
