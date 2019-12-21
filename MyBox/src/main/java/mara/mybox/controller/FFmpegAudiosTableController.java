package mara.mybox.controller;

import mara.mybox.data.VisitHistory;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-8
 * @Description
 * @License Apache License Version 2.0
 */
public class FFmpegAudiosTableController extends FFmpegMediasTableController {

    public FFmpegAudiosTableController() {
        SourceFileType = VisitHistory.FileType.Audio;
        SourcePathType = VisitHistory.FileType.Audio;
        TargetPathType = VisitHistory.FileType.Audio;
        TargetFileType = VisitHistory.FileType.Audio;
        AddFileType = VisitHistory.FileType.Audio;
        AddPathType = VisitHistory.FileType.Audio;

        sourceExtensionFilter = CommonFxValues.SoundExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

}
