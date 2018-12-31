package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;

/**
 * @Author Mara
 * @CreateDate 2018-7-6
 * @Description
 * @License Apache License Version 2.0
 */
public class DirectoriesTableController extends FilesTableController {

    protected DirectoriesRenameController renameController;

    @Override
    protected void initializeNext() {
        try {
            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));

            filesTableView.setItems(tableData);
            filesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            filesTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });
            checkTableSelected();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    void addAction(int index) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File defaultPath = new File(AppVaribles.getUserConfigValue(parentController.sourcePathKey, CommonValues.UserFilePath));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(CommonValues.UserFilePath);
            }
            chooser.setInitialDirectory(defaultPath);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setUserConfigValue(parentController.sourcePathKey, directory.getPath());

            if (findData(directory.getAbsolutePath()) != null) {
                return;
            }
            FileInformation d = new FileInformation(directory);
            if (index < 0 || index >= tableData.size()) {
                tableData.add(d);
            } else {
                tableData.add(index, d);
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    void recoveryAllAction(ActionEvent event) {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        renameController.recoveryAll();
        filesTableView.refresh();
        addButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    @FXML
    @Override
    void recoverySelectedAction(ActionEvent event) {
        ObservableList<FileInformation> selected = filesTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        renameController.recoverySelected(selected);
        filesTableView.refresh();
        addButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    public DirectoriesRenameController getRenameController() {
        return renameController;
    }

    public void setRenameController(DirectoriesRenameController renameController) {
        this.renameController = renameController;
    }

}
