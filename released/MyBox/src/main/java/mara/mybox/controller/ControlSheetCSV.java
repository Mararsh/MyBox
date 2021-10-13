package mara.mybox.controller;

import mara.mybox.db.data.VisitHistory;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 *
 * ControlSheetCSV < ControlSheetCSV_Calculation < ControlSheetCSV_Operations <
 * ControlSheetCSV_file < ControlSheetFile
 */
public class ControlSheetCSV extends ControlSheetCSV_Calculation {

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

}
