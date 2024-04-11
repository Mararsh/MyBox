package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-17
 * @License Apache License Version 2.0
 */
public class DataFileTextConvertController extends BaseDataFileConvertController {

    protected String sourceDelimiterName;
    protected Charset sourceCharset;
    protected boolean sourceWithName;

    @FXML
    protected ControlTextOptions readOptionsController;

    public DataFileTextConvertController() {
        baseTitle = Languages.message("TextDataConvert");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text, VisitHistory.FileType.All);
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();
            readOptionsController.setControls(baseName + "Read", true, true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (readOptionsController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())
                || (!readOptionsController.autoDetermine && readOptionsController.charset == null)) {
            return false;
        }
        sourceCharset = readOptionsController.charset;
        sourceDelimiterName = readOptionsController.getDelimiterName();
        sourceWithName = readOptionsController.withNamesCheck.isSelected();
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        String result;
        if (readOptionsController.autoDetermine) {
            sourceCharset = TextFileTools.charset(srcFile);
        }
        List<String> names = null;
        File validFile = FileTools.removeBOM(currentTask, srcFile);
        if (currentTask == null || !currentTask.isWorking()) {
            return message("Cancelled");
        }
        if (validFile == null) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(validFile, sourceCharset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (currentTask == null || currentTask.isCancelled()) {
                    return message("Cancelled");
                }
                List<String> rowData = TextTools.parseLine(line, sourceDelimiterName);
                if (rowData == null || rowData.isEmpty()) {
                    continue;
                }
                if (names == null) {
                    names = new ArrayList<>();
                    if (sourceWithName) {
                        names.addAll(rowData);
                    } else {
                        for (int i = 1; i <= rowData.size(); i++) {
                            names.add(message("Column") + i);
                        }
                    }
                    export.setNames(targetPathController, names, filePrefix(srcFile));
                    export.openWriters();
                    if (sourceWithName) {
                        continue;
                    }
                }
                export.writeRow(rowData);
            }
            export.closeWriters();
            result = message("Handled");
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

}
