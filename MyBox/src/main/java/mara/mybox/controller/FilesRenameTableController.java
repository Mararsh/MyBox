package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesRenameTableController extends FilesTableController {

    @FXML
    protected TableColumn<FileInformation, String> newColumn;
    @FXML
    protected Button recoveryAllButton, recoverySelectedButton, okRecoverButton;
    @FXML
    protected HBox confirmBox;

    public FilesRenameTableController() {
    }

    @Override
    public void initTable() {
        try {
            thisPane.getChildren().remove(confirmBox);
            super.initTable();

            newColumn.setCellValueFactory(new PropertyValueFactory<>("data"));
            okRecoverButton.disableProperty().bind(tableView.itemsProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void checkButtons() {
        if (thisPane.getChildren().contains(confirmBox)) {
            addFilesButton.setDisable(true);
            addDirectoryButton.setDisable(true);
            insertFilesButton.setDisable(true);
            insertDirectoryButton.setDisable(true);
            deleteFilesButton.setDisable(true);
            clearFilesButton.setDisable(true);
        } else {
            addFilesButton.setDisable(false);
            addDirectoryButton.setDisable(false);
            super.checkButtons();
        }
    }

    @FXML
    public void popRegexExample(MouseEvent mouseEvent) {
        popMenu = FxmlControl.popRegexExample(this, popMenu, tableFiltersInput, mouseEvent);
    }

    public void setButtonsAfterRenamed() {
        if (!thisPane.getChildren().contains(confirmBox)) {
            thisPane.getChildren().add(1, confirmBox);
            FxmlControl.refreshStyle(confirmBox);
        }
        checkButtons();
    }

    @FXML
    protected void recoveryAllAction(ActionEvent event) {
        if (parentController != null) {
            FilesRenameController p = (FilesRenameController) parentController;
            p.recoveryAllAction();
            tableView.refresh();
            if (thisPane.getChildren().contains(confirmBox)) {
                thisPane.getChildren().remove(confirmBox);
            }
            addFilesButton.setDisable(false);
            addDirectoryButton.setDisable(false);
            checkButtons();
        }
    }

    @FXML
    protected void recoverySelectedAction(ActionEvent event) {
        if (parentController != null) {
            FilesRenameController p = (FilesRenameController) parentController;
            p.recoverySelectedAction();
        }
        tableView.refresh();
        checkButtons();
    }

    @FXML
    protected void okAction(ActionEvent event) {
        if (parentController == null) {
            return;
        }
        FilesRenameController p = (FilesRenameController) parentController;
        p.okAction();
        tableView.refresh();
        if (thisPane.getChildren().contains(confirmBox)) {
            thisPane.getChildren().remove(confirmBox);
        }
        addFilesButton.setDisable(false);
        addDirectoryButton.setDisable(false);
        checkButtons();
    }

}
