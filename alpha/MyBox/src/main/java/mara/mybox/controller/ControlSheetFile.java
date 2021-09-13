package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 *
 * ControlSheetFile < ControlSheetFile_Sheet < ControlSheetFile_File <
 * ControlSheet
 */
public abstract class ControlSheetFile extends ControlSheetFile_Sheet {

    @Override
    public void initValues() {
        try {
            super.initValues();

            fileLoadedNotify = new SimpleBooleanProperty(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParent(BaseController parent) {
        if (parent != null) {
            this.parentController = parent;
            if (parent instanceof BaseDataFileController) {
                this.backupController = ((BaseDataFileController) parent).backupController;
            }
            this.baseName = parent.baseName;
            this.baseTitle = parent.baseTitle;
        }
        setControls();
    }

}
