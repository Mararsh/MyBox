package mara.mybox.controller;

import java.io.File;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchImageController extends BaseBatchFileController {

    protected ImageInformation imageInformation;

    public BaseBatchImageController() {
        targetSubdirKey = "ImageCreatSubdir";
        previewKey = "ImagePreview";
        browseTargets = true;
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    public void loadImageInformation(final File file) {
        sourceFile = file;
        imageInformation = null;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                public Void call() {
                    ImageFileInformation imageFileInformation
                            = ImageInformation.readImageFileInformation(file);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
