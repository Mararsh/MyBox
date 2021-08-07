package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesTableController extends BaseBatchTableController<FileInformation> {

    public FilesTableController() {
    }

    @Override
    public void initTable() {
        try {
            super.initTable();

            if (tableSubdirCheck != null) {
                tableSubdirCheck.setSelected(UserConfig.getBoolean("FileTableSubDir", true));
                tableSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("FileTableSubDir", tableSubdirCheck.isSelected());
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected FileInformation create(File file) {
        return new FileInformation(file);
    }

}
