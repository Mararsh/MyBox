package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */

/*
    T must be subClass of FileInformation
 */
public class FilesTableController extends TableController<FileInformation> {

    @FXML
    protected FlowPane selectPane;

    public FilesTableController() {
    }

    @Override
    public void initTable() {
        try {
            super.initTable();

            tableSubdirCheck.setSelected(AppVariables.getUserConfigBoolean("FileTableSubDir", true));
            tableSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("FileTableSubDir", tableSubdirCheck.isSelected());
                }
            });

            if (moreButton != null) {
                moreButton.setSelected(AppVariables.getUserConfigBoolean("FileTableMore", true));
                moreAction();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void moreAction() {
        if (moreButton.isSelected()) {
            if (!thisPane.getChildren().contains(selectPane)) {
                thisPane.getChildren().add(2, selectPane);
            }
            if (!thisPane.getChildren().contains(tableLabel)) {
                thisPane.getChildren().add(3, tableLabel);
            }
        } else {
            thisPane.getChildren().removeAll(selectPane, tableLabel);
        }
        FxmlControl.refreshStyle(thisPane);
        AppVariables.setUserConfigValue("FileTableMore", moreButton.isSelected());
    }

    @Override
    protected FileInformation create(File file) {
        return new FileInformation(file);
    }

}
