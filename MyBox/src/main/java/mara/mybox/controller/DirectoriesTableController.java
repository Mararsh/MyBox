package mara.mybox.controller;

import java.io.File;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.FileInformation;

/**
 * @Author Mara
 * @CreateDate 2018-7-6
 * @Description
 * @License Apache License Version 2.0
 */
public class DirectoriesTableController extends FilesTableController {

    protected DirectoriessRenameController renameController;

    @Override
    protected void initializeNext() {
        try {
            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));

            filesTableView.setItems(tableData);
            filesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    void addAction(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File defaultPath = new File(AppVaribles.getConfigValue(parentController.sourcePathKey, System.getProperty("user.home")));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(defaultPath);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setConfigValue(parentController.sourcePathKey, directory.getPath());

            if (findData(directory.getAbsolutePath()) != null) {
                return;
            }
            FileInformation d = new FileInformation(directory);
            tableData.add(d);
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

    public DirectoriessRenameController getRenameController() {
        return renameController;
    }

    public void setRenameController(DirectoriessRenameController renameController) {
        this.renameController = renameController;
    }

}
