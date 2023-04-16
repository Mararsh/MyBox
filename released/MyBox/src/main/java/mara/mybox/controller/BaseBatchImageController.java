package mara.mybox.controller;

import java.io.File;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.SingletonTask;
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
            task = new SingletonTask<Void>(this) {

                @Override
                public Void call() {
                    ImageFileInformation imageFileInformation = ImageFileInformation.create(file);
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
            start(task);
        }
    }

    public void afterImageInfoLoaded() {

    }

    @Override
    public boolean matchType(File file) {
        return FileTools.isSupportedImage(file);
    }

}
