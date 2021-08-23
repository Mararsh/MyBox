package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController extends BaseDataFileController_File {

    public BaseDataFileController() {
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            currentPageStart = 1;
            currentPage = 1;
            pageSize = 50;
            sourceWithNames = totalRead = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initBackupsTab();
            initSaveAsTab();
            initPagination();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void checkRightPaneHide() {
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        newSheet(3, 3);
    }

}
