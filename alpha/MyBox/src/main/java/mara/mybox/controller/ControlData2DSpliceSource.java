package mara.mybox.controller;

import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-5-22
 * @License Apache License Version 2.0
 */
public class ControlData2DSpliceSource extends BaseData2DSourceController {

    @Override
    public void setFileType() {
        setFileType(FileType.DataFile);
    }

    @Override
    public void loadData() {
        super.loadData();
        filterController.setData2D(data2D);
    }

    @Override
    public boolean validateData() {
        boolean invalid = data2D == null || !data2D.isColumnsValid();
        dataBox.setDisable(invalid);
        editButton.setDisable(invalid);
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

}
