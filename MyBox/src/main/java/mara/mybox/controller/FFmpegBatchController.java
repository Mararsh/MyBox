package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data.VisitHistory;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegBatchController extends FilesBatchController {

    @FXML
    protected FFmpegOptionsController ffmpegOptionsController;

    public FFmpegBatchController() {
        baseTitle = AppVariables.message("MediaInformation");

        SourceFileType = VisitHistory.FileType.Media;
        SourcePathType = VisitHistory.FileType.Media;
        TargetPathType = VisitHistory.FileType.Media;
        TargetFileType = VisitHistory.FileType.Media;
        AddFileType = VisitHistory.FileType.Media;
        AddPathType = VisitHistory.FileType.Media;

        targetPathKey = "MediaFilePath";
        sourcePathKey = "MediaFilePath";

        sourceExtensionFilter = CommonFxValues.FFmpegMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

}
