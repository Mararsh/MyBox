package mara.mybox.controller;

import java.io.File;
import mara.mybox.image.data.ImageFileInformation;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.FxSingletonTask;

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
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    public void loadImageInformation(final File file) {
        if (task != null && !task.isQuit()) {
            return;
        }
        sourceFile = file;
        imageInformation = null;
        task = new FxSingletonTask<Void>(this) {

            @Override
            public Void call() {
                ImageFileInformation imageFileInformation
                        = ImageFileInformation.create(this, file);
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

    public void afterImageInfoLoaded() {

    }

}
