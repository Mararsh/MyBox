package mara.mybox.controller;

import java.io.File;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.image.file.ImageTiffFile;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTiffEditerController extends BaseImagesListController {

    public ImageTiffEditerController() {
        baseTitle = AppVariables.message("ImageTiffEditer");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Tif);
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;
    }

    @Override
    public void saveFileDo(final File outFile) {

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    error = ImageTiffFile.writeWithInfo(tableData, null, outFile);
                    return error.isEmpty();
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    if (outFile.equals(sourceFile)) {
                        setImageChanged(false);
                    }
                    if (viewCheck.isSelected()) {
                        final ImageFramesViewerController controller
                                = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
                        controller.selectSourceFile(outFile);
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        }
    }

}
