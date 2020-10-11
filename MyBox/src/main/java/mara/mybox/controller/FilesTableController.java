package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mara.mybox.data.FileInformation;
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
public class FilesTableController extends BatchTableController<FileInformation> {

    public FilesTableController() {
    }

    @Override
    public void initTable() {
        try {
            super.initTable();

            if (tableSubdirCheck != null) {
                tableSubdirCheck.setSelected(AppVariables.getUserConfigBoolean("FileTableSubDir", true));
                tableSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("FileTableSubDir", tableSubdirCheck.isSelected());
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected FileInformation create(File file) {
        return new FileInformation(file);
    }

}
