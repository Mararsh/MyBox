package mara.mybox.controller.base;

import java.io.File;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Modality;
import mara.mybox.controller.FilesBatchController;
import mara.mybox.data.VisitHistory;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImagesBatchController extends FilesBatchController {

    protected ImageInformation imageInformation;

    public ImagesBatchController() {
        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = "ImageTargetPath";
        targetSubdirKey = "ImageCreatSubdir";
        previewKey = "ImagePreview";
        sourcePathKey = "ImageSourcePath";

        browseTargets = true;

        sourceExtensionFilter = CommonValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    public void loadImageInformation(final File file) {
        sourceFile = file;
        imageInformation = null;
        task = new Task<Void>() {
            private boolean ok;

            @Override
            public Void call() {
                ImageFileInformation imageFileInformation
                        = ImageInformation.loadImageFileInformation(file);
                if (imageFileInformation == null
                        || imageFileInformation.getImagesInformation() == null) {
                    return null;
                }
                imageInformation = imageFileInformation.getImageInformation();

                ok = true;
                return null;
            }

            @Override
            public void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            afterImageInfoLoaded();
                        }
                    });
                }
            }

            @Override
            public void cancelled() {
                super.cancelled();
            }

            @Override
            public void failed() {
                super.failed();
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void afterImageInfoLoaded() {

    }

}
