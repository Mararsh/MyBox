package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseChildController extends BaseFileController {

    @Override
    public void initValues() {
        try {
            super.initValues();

            stageType = StageType.Child;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
