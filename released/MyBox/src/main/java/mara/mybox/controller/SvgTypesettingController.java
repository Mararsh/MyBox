package mara.mybox.controller;

import mara.mybox.db.data.VisitHistory;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-16
 * @License Apache License Version 2.0
 */
public class SvgTypesettingController extends XmlTypesettingController {

    public SvgTypesettingController() {
        baseTitle = message("SvgTypesetting");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.SVG);
    }

}
