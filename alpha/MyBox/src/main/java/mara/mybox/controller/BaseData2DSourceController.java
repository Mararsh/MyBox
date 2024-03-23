package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class BaseData2DSourceController extends BaseData2DSelectRowsController {

    @Override
    public void initControls() {
        try {
            super.initControls();

            selectColumnsInTable = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void updateInterface() {
        if (data2D != null && data2D.isDataFile() && data2D.getFile() != null) {
            if (!toolbar.getChildren().contains(fileMenuButton)) {
                toolbar.getChildren().add(2, fileMenuButton);
            }
        } else {
            if (toolbar.getChildren().contains(fileMenuButton)) {
                toolbar.getChildren().remove(fileMenuButton);
            }
        }
        super.updateInterface();
    }

}
