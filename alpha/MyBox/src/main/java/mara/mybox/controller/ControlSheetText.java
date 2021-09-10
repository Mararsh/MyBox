package mara.mybox.controller;

import mara.mybox.db.data.VisitHistory;

/**
 * @Author Mara
 * @CreateDate 2021-9-6
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetText extends ControlSheetFile {

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

}
