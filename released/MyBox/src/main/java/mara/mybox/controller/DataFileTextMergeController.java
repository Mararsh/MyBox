package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-17
 * @License Apache License Version 2.0
 */
public class DataFileTextMergeController extends FilesMergeController {

    protected String sourceDelimiterName, targetDelimiter;
    protected Charset sourceCharset, targetCharset;
    protected BufferedWriter writer;
    protected List<String> headers;
    protected boolean sourceWithName, targetWithName;

    @FXML
    protected ControlTextOptions readOptionsController, writeOptionsController;

    public DataFileTextMergeController() {
        baseTitle = message("TextDataMerge");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableData)
                    .or(targetFileController.valid.not())
                    .or(readOptionsController.delimiterController.delimiterInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(writeOptionsController.delimiterController.delimiterInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            readOptionsController.setControls(baseName + "Read", true, true);
            writeOptionsController.setControls(baseName + "Write", false, true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected boolean openWriter() {
        try {
            sourceCharset = readOptionsController.charset;
            sourceDelimiterName = readOptionsController.getDelimiterName();
            sourceWithName = readOptionsController.withNamesCheck.isSelected();

            targetCharset = writeOptionsController.charset;
            targetDelimiter = TextTools.delimiterValue(writeOptionsController.getDelimiterName());
            targetWithName = writeOptionsController.withNamesCheck.isSelected();
            writer = new BufferedWriter(new FileWriter(targetFile, targetCharset));

            headers = null;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile) {
        if (readOptionsController.autoDetermine) {
            sourceCharset = TextFileTools.charset(srcFile);
        }
        String result;
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
                if (currentTask == null || !currentTask.isWorking()) {
                    return message("Cancelled");
                }
                List<String> rowData = TextTools.parseLine(line, sourceDelimiterName);
                if (rowData == null || rowData.isEmpty()) {
                    continue;
                }
                if (headers == null) {
                    if (sourceWithName) {
                        headers = rowData;
                    } else {
                        headers = new ArrayList<>();
                        for (int i = 1; i <= rowData.size(); i++) {
                            headers.add(message("Column") + i);
                        }
                    }
                    if (targetWithName) {
                        TextFileTools.writeLine(currentTask, writer, headers, targetDelimiter);
                    }
                    if (sourceWithName) {
                        continue;
                    }
                }
                TextFileTools.writeLine(currentTask, writer, rowData, targetDelimiter);
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
            writer.flush();
            writer.close();
            try (Connection conn = DerbyBase.getConnection()) {
                TableData2DDefinition tableData2DDefinition = new TableData2DDefinition();
                Data2DDefinition def = tableData2DDefinition.queryFile(conn, Data2DDefinition.Type.Texts, targetFile);
                if (def == null) {
                    def = Data2DDefinition.create();
                }
                def.setType(Data2DDefinition.Type.Texts)
                        .setFile(targetFile)
                        .setDataName(targetFile.getName())
                        .setCharset(targetCharset)
                        .setHasHeader(targetWithName)
                        .setDelimiter(targetDelimiter);
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

    @Override
    public void viewTarget(File file) {
        if (file == null) {
            return;
        }
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.sourceFileChanged(file);
    }

}
