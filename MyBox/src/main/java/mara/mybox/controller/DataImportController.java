package mara.mybox.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
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
import javafx.scene.control.TabPane;
import mara.mybox.data.FileInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableBase;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-04-03
 * @License Apache License Version 2.0
 */
public class DataImportController<D> extends FilesBatchController {

    protected DataAnalysisController parent;
    protected TableBase tableDefinition;

    @FXML
    protected CheckBox replaceCheck, statisticCheck, closeWhenCompleteCheck;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab sourcesTab, commentsTab;
    @FXML
    protected Hyperlink link;
    @FXML
    protected ControlCSVEdit csvEditController;

    public DataImportController() {
        baseTitle = AppVariables.message("ImportEpidemicReport");

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);

        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
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
                FxmlControl.removeTooltip(csvEditController.inputButton);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setLink() {
        link.setText(CommonValues.MyBoxInternetDataPath
                + (AppVariables.isChinese() ? "" : "/tree/master/en"));
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
            logger.debug(e.toString());
        }
    }

    /*
        methods need implemented
     */
    public TableBase getTableDefinition() {
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
                return AppVariables.message("Canceled");
            }
            if (srcFile == null || !srcFile.isFile()) {
                return AppVariables.message("Skip");
            }
            countHandling(srcFile);
            long count = importFile(srcFile);
            if (count >= 0) {
                totalItemsHandled += count;
                return AppVariables.message("Successful");
            } else {
                return AppVariables.message("Failed");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    public long importFile(File file) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            long ret = importFile(conn, file);
            conn.commit();
            return ret;
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return -1;
    }

    public long importFile(Connection conn, File file) {
        if (getTableDefinition() == null) {
            return -1;
        }
//        if (tableDefinition.isSupportBatchUpdate()) {
//            return importFileBatch(conn, file);
//        }
        long importCount = 0, insertCount = 0, updateCount = 0, skipCount = 0, failedCount = 0;
        try ( CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
            List<String> names = parser.getHeaderNames();
            if ((!validHeader(names))) {
                updateLogs(message("InvalidFormat"), true);
                return -1;
            }
            try ( PreparedStatement insert = conn.prepareStatement(insertStatement());
                     PreparedStatement update = conn.prepareStatement(updateStatement())) {
                conn.setAutoCommit(false);
                int line = 0;
                for (CSVRecord record : parser) {
                    if (task == null || task.isCancelled()) {
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
                                    updateLogs(message("Update") + ": " + updateCount + " "
                                            + dataValues(data), true);
                                }
                            } else {
                                ++failedCount;
                                updateLogs(message("Failed") + ": " + failedCount + " "
                                        + dataValues(data), true);
                            }
                        } else {
                            skipCount++;
                            if (verboseCheck.isSelected() || (skipCount % 100 == 0)) {
                                updateLogs(message("Skip") + ": " + skipCount + " "
                                        + dataValues(data), true);
                            }
                        }
                    } else {
                        if (insert(conn, insert, data)) {
                            insertCount++;
                            importCount++;
                            if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                updateLogs(message("Insert") + ": " + insertCount + " "
                                        + dataValues(data), true);
                            }
                        } else {
                            ++failedCount;
                            updateLogs(message("Failed") + ": " + failedCount + " "
                                    + dataValues(data), true);
                        }
                    }
                    if (importCount % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            updateLogs(e.toString(), true);
        }
        updateLogs(message("Imported") + ":" + importCount + "  " + file + "\n"
                + message("Insert") + ":" + insertCount + " "
                + message("Update") + ":" + updateCount + " "
                + message("FailedCount") + ":" + failedCount + " "
                + message("Skipped") + ":" + skipCount, true);
        return importCount;
    }

    // commit update in batch
    public long importFileBatch(Connection conn, File file) {
        // To be implemented
        return -1;
    }

    @Override
    public void donePost() {
        super.donePost();
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
    public void setTableDefinition(TableBase tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

}
