package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-5-22
 * @License Apache License Version 2.0
 */
public class ControlData2DSpliceSource extends BaseData2DSourceController {

    @FXML
    protected Button editDataButton;

    @Override
    public void setFileType() {
        setFileType(FileType.DataFile);
    }

    @Override
    public boolean loadDef(Data2DDefinition def) {
        if (!super.loadDef(def)) {
            return false;
        }
        filterController.setData2D(data2D);
        return true;
    }

    @Override
    public void sourceFileChanged(File file) {
        try {
            resetStatus();
            setData(Data2D.create(Data2DDefinition.type(file)));
            data2D.initFile(file);
            readDefinition();
            filterController.setData2D(data2D);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean validateData() {
        boolean invalid = data2D == null || !data2D.isColumnsValid();
        dataBox.setDisable(invalid);
        editDataButton.setDisable(invalid);
        return !invalid;
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            if (data2D != null) {
                setLabel(data2D.displayName());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void editAction() {
        Data2DDefinition.open(data2D);
    }

}
