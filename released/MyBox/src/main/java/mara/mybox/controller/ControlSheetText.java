package mara.mybox.controller;

import mara.mybox.db.data.VisitHistory;

/**
 * @Author Mara
 * @CreateDate 2021-9-6
 * @License Apache License Version 2.0
 *
 * ControlSheetText < ControlSheetText_Calculation < ControlSheetText_Operations
 * < ControlSheetText_File < ControlSheetFile
 */
public class ControlSheetText extends ControlSheetText_Calculation {

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

}
