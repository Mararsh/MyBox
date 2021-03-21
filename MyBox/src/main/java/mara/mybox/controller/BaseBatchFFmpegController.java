package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchFFmpegController extends BaseBatchFileController {

    @FXML
    protected ControlFFmpegOptions ffmpegOptionsController;

    public BaseBatchFFmpegController() {
        baseTitle = AppVariables.message("MediaInformation");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Media);
        sourceExtensionFilter = CommonFxValues.FFmpegMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

}
