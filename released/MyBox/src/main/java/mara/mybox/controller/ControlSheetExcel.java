package mara.mybox.controller;

import mara.mybox.db.data.VisitHistory;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 *
 * ControlSheetExcel < ControlSheetExcel_Calculation <
 * ControlSheetExcel_Operations < ControlSheetExcel_File < ControlSheetFile
 */
public class ControlSheetExcel extends ControlSheetExcel_Calculation {

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

}
