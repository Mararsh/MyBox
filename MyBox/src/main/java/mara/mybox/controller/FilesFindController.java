package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.TableFileSizeCell;
import mara.mybox.fxml.TableTimeCell;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-9
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesFindController extends FilesBatchController {

    protected ObservableList<FileInformation> filesList;
    protected long totalChecked, totalMatched;

    @FXML
    protected TableView<FileInformation> filesView;
    @FXML
    protected TableColumn<FileInformation, String> fileColumn, typeColumn;
    @FXML
    protected TableColumn<FileInformation, Long> sizeColumn, modifyTimeColumn, createTimeColumn;

    public FilesFindController() {
        baseTitle = AppVariables.message("FilesFind");
        allowPaused = false;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            initFilesTab();

            tableController.listButton.setVisible(false);
            openTargetButton.setVisible(false);
            openCheck.setVisible(false);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initFilesTab() {
        try {
            filesList = FXCollections.observableArrayList();
            filesView.setItems(filesList);

            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            fileColumn.setPrefWidth(400);

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSuffix"));

            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
            sizeColumn.setCellFactory(new TableFileSizeCell());

            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifyTimeColumn.setCellFactory(new TableTimeCell());

            createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            createTimeColumn.setCellFactory(new TableTimeCell());

            filesView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        int index = filesView.getSelectionModel().getSelectedIndex();
                        if (index < 0 || index > filesList.size() - 1) {
                            return;
                        }
                        FileInformation info = filesList.get(index);
                        view(info.getFile());
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public boolean makeBatchParameters() {
        filesList.clear();
        filesView.refresh();
        totalChecked = totalMatched = 0;
        return super.makeBatchParameters();
    }

    @Override
    public void disableControls(boolean disable) {
        tableController.thisPane.setDisable(disable);
        batchTabPane.getSelectionModel().select(targetTab);
    }

    @Override
    public String handleFile(File file) {
        try {
            totalChecked++;
            if (!match(file)) {
                return AppVariables.message("Done");
            }
            totalMatched++;
            filesList.add(new FileInformation(file));
            return AppVariables.message("Done");
        } catch (Exception e) {
            return AppVariables.message("Done");
        }
    }

    @Override
    public String handleDirectory(File directory) {
        try {
            if (directory == null || !directory.isDirectory()) {
                return AppVariables.message("Done");
            }
            File[] files = directory.listFiles();
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return AppVariables.message("Done");
                }
                if (srcFile.isFile()) {
                    totalChecked++;
                    if (!match(srcFile)) {
                        continue;
                    }
                    totalMatched++;
                    filesList.add(new FileInformation(srcFile));
                } else if (srcFile.isDirectory()) {
                    handleDirectory(srcFile);
                }
            }
            return AppVariables.message("Done");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Done");
        }
    }

    @Override
    public void showCost() {
        if (operationBarController.getStatusLabel() == null) {
            return;
        }
        long cost = new Date().getTime() - processStartTime.getTime();
        String s;
        if (paused) {
            s = message("Paused");
        } else {
            s = message(currentParameters.status);
        }
        s += ".  "
                + message("TotalCheckedFiles") + ": " + totalChecked + "   "
                + message("TotalMatched") + ": " + totalMatched + ".   "
                + message("Cost") + ": " + DateTools.showTime(cost) + ". "
                + message("StartTime") + ": " + DateTools.datetimeToString(processStartTime) + ", "
                + message("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.getStatusLabel().setText(s);
    }

    @Override
    public void donePost() {
        showCost();
        if (operationBarController.miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {

    }

}
