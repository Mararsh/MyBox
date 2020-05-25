package mara.mybox.controller;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import mara.mybox.data.FileInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-04-03
 * @License Apache License Version 2.0
 */
public class DataImportController extends FilesBatchController {

    protected DataAnalysisController parent;

    @FXML
    protected CheckBox replaceCheck, statisticCheck;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab sourcesTab, commentsTab;

    public DataImportController() {
        baseTitle = AppVariables.message("ImportEpidemicReport");

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        targetPathKey = "TextFilePath";
        sourcePathKey = "TextFilePath";

        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
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

    public void startFile(File file, boolean replace) {
        isSettingValues = true;
        tableData.clear();
        tableData.add(new FileInformation(file));
        tableView.refresh();
        isSettingValues = false;
        replaceCheck.setSelected(replace);
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
        return -1;
    }

    @Override
    public void donePost() {
        super.donePost();
        if (parent != null && parent.getMyStage().isShowing()) {
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        timer = null;
                        if (parent != null) {
                            parent.refreshAction();
                        }
                    });
                }

            }, 500);

        }
    }
}
