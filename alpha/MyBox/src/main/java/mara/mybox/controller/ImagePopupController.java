package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-9-18
 * @License Apache License Version 2.0
 */
public class ImagePopupController extends ImageViewerController {

    public ImagePopupController() {
        baseTitle = Languages.message("ImageViewer");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            saveAsType = SaveAsType.Open;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
