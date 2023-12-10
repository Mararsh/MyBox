package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import mara.mybox.data.FileInformation;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-04-03
 * @License Apache License Version 2.0
 */
public abstract class BaseImportCsvController<D> extends BaseBatchFileController {

    protected BaseDataManageController parent;
    protected BaseTable tableDefinition;

    @FXML
    protected CheckBox replaceCheck, statisticCheck, closeWhenCompleteCheck;
    @FXML
    protected Tab sourcesTab, commentsTab;
    @FXML
    protected Hyperlink link;
    @FXML
    protected ControlCSVEdit csvEditController;

    public BaseImportCsvController() {
        baseTitle = Languages.message("ImportEpidemicReport");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        super.initControls();
        if (link != null) {
            setLink();
        }
        if (csvEditController != null) {
            csvEditController.init(this, getTableDefinition());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            if (csvEditController != null) {
                NodeStyleTools.removeTooltip(csvEditController.inputButton);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setLink() {
        link.setText("https://github.com/Mararsh/MyBox_data"
                + (Languages.isChinese() ? "" : "/tree/master/md/en"));
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            if (tableView != null) {
                startButton.disableProperty().bind(
                        Bindings.isEmpty(tableView.getItems())
                );
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        methods need implemented
     */
    public BaseTable getTableDefinition() {
        return tableDefinition;
    }

    protected boolean validHeader(List<String> names) {
        return true;
    }

    protected String insertStatement() {
        if (getTableDefinition() == null) {
            return null;
        }
        return tableDefinition.insertStatement();
    }

    protected String updateStatement() {
        if (getTableDefinition() == null) {
            return null;
        }
        return tableDefinition.updateStatement();
    }

    protected D readRecord(Connection conn, List<String> names, CSVRecord record) {
        return null;
    }

    protected D readData(Connection conn, D data) {
        if (getTableDefinition() == null) {
            return null;
        }
        return (D) (tableDefinition.readData(conn, data));
    }

    protected String dataValues(D data) {
        return "";
    }

    protected boolean update(Connection conn, PreparedStatement updateStatement, D data) {
        return tableDefinition.updateData(conn, updateStatement, data) != null;
    }

    protected boolean insert(Connection conn, PreparedStatement insertStatement, D data) {
        return tableDefinition.insertData(conn, insertStatement, data) != null;
    }

    /*
        general method may need not change
     */
    public void startFile(File file, boolean replace, boolean closeOnComplete) {
        isSettingValues = true;
        tableData.clear();
        tableData.add(new FileInformation(file));
        tableView.refresh();
        isSettingValues = false;
        if (replaceCheck != null) {
            replaceCheck.setSelected(replace);
        }
        if (closeWhenCompleteCheck != null) {
            closeWhenCompleteCheck.setSelected(closeOnComplete);
        }
        startAction();
    }

    @Override
    public void doCurrentProcess() {
        super.doCurrentProcess();
        if (tabPane != null && logsTab != null) {
            tabPane.getSelectionModel().select(logsTab);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            if (task == null || task.isCancelled()) {
                return Languages.message("Canceled");
            }
            if (srcFile == null || !srcFile.isFile()) {
                return Languages.message("Skip");
            }
            long count = importFile(srcFile);
            if (count >= 0) {
                totalItemsHandled += count;
                return Languages.message("Successful");
            } else {
                return Languages.message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return Languages.message("Failed");
        }
    }

    public long importFile(File file) {
        try (Connection conn = DerbyBase.getConnection()) {
            long ret = importFile(task, conn, file);
            conn.commit();
            return ret;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return -1;
    }

    public long importFile(FxTask task, Connection conn, File file) {
        if (getTableDefinition() == null) {
            return -1;
        }
//        if (tableDefinition.isSupportBatchUpdate()) {
//            return importFileBatch(conn, file);
//        }
        long importCount = 0, insertCount = 0, updateCount = 0, skipCount = 0, failedCount = 0;
        File validFile = FileTools.removeBOM(task, file);
        if (validFile == null || (task != null && !task.isWorking())) {
            return -1;
        }
        try (CSVParser parser = CSVParser.parse(validFile, TextFileTools.charset(file), CsvTools.csvFormat())) {
            List<String> names = parser.getHeaderNames();
            if ((!validHeader(names))) {
                updateLogs(Languages.message("InvalidFormat"), true);
                return -1;
            }
            try (PreparedStatement insert = conn.prepareStatement(insertStatement());
                    PreparedStatement update = conn.prepareStatement(updateStatement())) {
                conn.setAutoCommit(false);
                int line = 0;
                for (CSVRecord record : parser) {
                    if (task == null || !task.isWorking()) {
                        conn.commit();
                        updateLogs("Canceled", true);
                        return importCount;
                    }
                    line++;
                    D data = readRecord(conn, names, record);
                    if (data == null) {
                        continue;
                    }
                    D exist = readData(conn, data);
                    if (exist != null) {
                        if (replaceCheck.isSelected()) {
                            tableDefinition.setId(exist, data);
                            if (update(conn, update, data)) {
                                updateCount++;
                                importCount++;
                                if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                    updateLogs(Languages.message("Update") + ": " + updateCount + " "
                                            + dataValues(data), true);
                                }
                            } else {
                                ++failedCount;
                                updateLogs(Languages.message("Failed") + ": " + failedCount + " "
                                        + dataValues(data), true);
                            }
                        } else {
                            skipCount++;
                            if (verboseCheck.isSelected() || (skipCount % 100 == 0)) {
                                updateLogs(Languages.message("Skip") + ": " + skipCount + " "
                                        + dataValues(data), true);
                            }
                        }
                    } else {
                        if (insert(conn, insert, data)) {
                            insertCount++;
                            importCount++;
                            if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                updateLogs(Languages.message("Insert") + ": " + insertCount + " "
                                        + dataValues(data), true);
                            }
                        } else {
                            ++failedCount;
                            updateLogs(Languages.message("Failed") + ": " + failedCount + " "
                                    + dataValues(data), true);
                        }
                    }
                    if (importCount % Database.BatchSize == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            showLogs(e.toString());
        }
        updateLogs(Languages.message("Imported") + ":" + importCount + "  " + file + "\n"
                + Languages.message("Insert") + ":" + insertCount + " "
                + Languages.message("Update") + ":" + updateCount + " "
                + Languages.message("FailedCount") + ":" + failedCount + " "
                + Languages.message("Skipped") + ":" + skipCount, true);
        return importCount;
    }

    // commit update in batch
    public long importFileBatch(Connection conn, File file) {
        // To be implemented
        return -1;
    }

    @Override
    public void afterTask() {
        super.afterTask();
        if (parent != null && parent.getMyStage().isShowing()) {
            if (closeWhenCompleteCheck != null && closeWhenCompleteCheck.isSelected()) {
                closeStage();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        timer = null;
                        if (parent != null) {
                            parent.refreshAction();
                            parent.toFront();
                        }
                    });
                }

            }, 500);

        }
    }

    /*
        get/set
     */
    public void setTableDefinition(BaseTable tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

}
