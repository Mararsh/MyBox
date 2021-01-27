package mara.mybox.controller;

import java.io.File;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseImagesBatchController extends FilesBatchController {

    protected ImageInformation imageInformation;

    public BaseImagesBatchController() {
        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);
        targetSubdirKey = "ImageCreatSubdir";
        previewKey = "ImagePreview";
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);

        browseTargets = true;

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    public void loadImageInformation(final File file) {
        sourceFile = file;
        imageInformation = null;
        synchronized (this) {
            if (task != null && !task.isQuit() ) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                public Void call() {
                    ImageFileInformation imageFileInformation
                            = ImageInformation.loadImageFileInformation(file);
                    if (imageFileInformation == null
                            || imageFileInformation.getImageInformation() == null) {
                        return null;
                    }
                    imageInformation = imageFileInformation.getImageInformation();

                    ok = true;
                    return null;
                }

                @Override
                protected void whenSucceeded() {
                    afterImageInfoLoaded();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void afterImageInfoLoaded() {

    }

    @Override
    public boolean matchType(File file) {
        return FileTools.isSupportedImage(file);
    }

}
